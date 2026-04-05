package com.quant.finance.execution.error;

import com.quant.finance.execution.client.IBClient;
import com.quant.finance.execution.entity.OrderEntity;
import com.quant.finance.execution.service.NotificationService;
import com.quant.finance.execution.service.OrderService;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class IBErrorHandler {

  @Lazy
  @Autowired
  private IBClient ibClient;
  private final OrderService orderService;
  private final NotificationService notificationService;

  private final AtomicBoolean connectionHealthy = new AtomicBoolean(false);
  private final AtomicBoolean reconnectInProgress = new AtomicBoolean(false);
  private final AtomicLong lastNotificationTime = new AtomicLong(0);
  private static final long NOTIFICATION_COOLDOWN_MS = 60_000;

  private static final Set<Integer> CONNECTION_OK_CODES = Set.of(2104, 2106, 2158);
  private static final Set<Integer> INFO_CODES = Set.of(2107, 2108, 10186);
  private static final Set<Integer> WARNING_CODES =
      Set.of(200, 201, 202, 321, 326, 399, 503, 504, 507, 1100, 1101, 1102, 2110, 10147);

  private static final Set<Integer> RETRIABLE_CONNECTION_ERROR_CODES = Set.of(502, 1300);

  public void handleError(int orderId, long reqId, int code, String message, String advancedMsg) {

    String errorText = String.format("orderId=%d, code=%d, message=%s", orderId, code, message);

    if (CONNECTION_OK_CODES.contains(code)) {
      handleConnectionOk();
      return;
    }

    if (INFO_CODES.contains(code)) {
      log.info("IB INFO: {}", errorText);
      return;
    }

    if (WARNING_CODES.contains(code)) {
      log.warn("IB WARNING: {}", errorText);
      setErrorMessageToOrder(orderId, errorText);
      notifyThrottled(errorText);
      return;
    }

    // --- CONNECTION ERROR ---
    if (RETRIABLE_CONNECTION_ERROR_CODES.contains(code)) {
      handleConnectionError(errorText);
      return;
    }

    log.error("IB ERROR: {}", errorText);
    setErrorMessageToOrder(orderId, errorText);
    notifyThrottled(errorText);
  }

  private void handleConnectionOk() {
    if (connectionHealthy.compareAndSet(false, true)) {
      log.info("IB connection is healthy");
    }
  }

  private void handleConnectionError(String errorText) {
    log.error("IB CONNECTION ERROR: {}", errorText);

    connectionHealthy.set(false);
    notifyThrottled(errorText);

    triggerReconnect();
  }

  private void triggerReconnect() {
    if (!reconnectInProgress.compareAndSet(false, true)) {
      log.warn("Reconnect already in progress, skipping...");
      return;
    }

    new Thread(() -> {
      try {
        log.info("Attempting IB reconnect...");
        ibClient.disconnect();
        Thread.sleep(600000);
        ibClient.connect();
      } catch (Exception e) {
        log.error("Reconnect failed", e);
      } finally {
        reconnectInProgress.set(false);
      }
    }).start();
  }

  private void notifyThrottled(String message) {
    long now = System.currentTimeMillis();
    long last = lastNotificationTime.get();

    if (now - last > NOTIFICATION_COOLDOWN_MS) {
      if (lastNotificationTime.compareAndSet(last, now)) {
        notificationService.notify(message);
      }
    }
  }

  private void setErrorMessageToOrder(int orderId, String errorText) {
    if (orderId <= 0) {
      return;
    }

    Optional<OrderEntity> optionalOrder = orderService.findByBrokerOrderId(orderId);

    optionalOrder.ifPresentOrElse(order -> {
      order.setErrorMessage(errorText);
      orderService.save(order);
    }, () -> log.warn("Order not found for id={}", orderId));
  }
}