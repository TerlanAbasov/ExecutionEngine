package com.quant.finance.execution.error;

import com.quant.finance.execution.client.IBClient;
import com.quant.finance.execution.entity.OrderEntity;
import com.quant.finance.execution.service.NotificationService;
import com.quant.finance.execution.service.OrderService;
import java.util.List;
import java.util.Optional;
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

  private static boolean isConnectionOk = false;

  public void handleError(int orderId, long l, int code, String message, String s1) {
    String errorText = String.format("orderId=%d, code=%d, message=%s", orderId, code, message);

    if (List.of(2104, 2158, 2106).contains(code)) {
      if (!isConnectionOk) {
        log.info("Connections are OK!");
      }

      isConnectionOk = true;
    } else if (List.of(2107, 2108, 1101, 1102).contains(code)) {
      log.info("INFO. {}", errorText);
    } else if (List.of(502, 504, 507, 1100, 2110).contains(code)) {
      log.error("CONNECTION ERROR. {}", errorText);
      ibClient.reconnectWithSleep();
    } else if (List.of(1100, 2110).contains(code)) {
      log.error("CONNECTION ERROR. {}", errorText);
      ibClient.reconnect();
    } else if (code == 326) {
      log.info("ERROR. {}", errorText);
      notificationService.notify(errorText);
    } else if (code == 399) {
      log.info("ERROR. {}", errorText);
    } else {
      log.error("ERROR. {}", errorText);
    }
  }

  private void setErrorMessageToOrder(int orderId, String errorText) {
    Optional<OrderEntity> optionalOrder = orderService.findByBrokerOrderId(orderId);

    if (optionalOrder.isPresent()) {
      optionalOrder.get().setErrorMessage(errorText);
      orderService.save(optionalOrder.get());
    } else {
      log.error("Order not fount with id={}", orderId);
    }
  }
}
