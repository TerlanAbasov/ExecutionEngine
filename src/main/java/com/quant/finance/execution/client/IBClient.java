package com.quant.finance.execution.client;

import static java.util.concurrent.ThreadLocalRandom.current;

import com.ib.client.Contract;
import com.ib.client.EClientSocket;
import com.ib.client.EJavaSignal;
import com.ib.client.EReader;
import com.ib.client.Order;
import com.ib.client.OrderCancel;
import com.ib.client.Util;
import com.quant.finance.execution.config.ApplicationProperties;
import com.quant.finance.execution.service.EWrapperImpl;
import com.quant.finance.execution.service.NotificationService;
import com.quant.finance.execution.service.OrderService;
import jakarta.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class IBClient {
  private static final AtomicInteger orderIds =
      new AtomicInteger(current().nextInt(1, Integer.MAX_VALUE));
  private static EClientSocket eClientSocket = null;

  private final EWrapperImpl eWrapper;
  private final NotificationService notificationService;
  private final ApplicationProperties properties;
  private static OrderService orderService;

  @Autowired
  public void setOrderRepository(OrderService orderService) {
    IBClient.orderService = orderService;
  }

  @PostConstruct
  public void connect() {
    EJavaSignal signal = new EJavaSignal();
    eClientSocket = new EClientSocket(eWrapper, signal);

    log.info("Connecting to {}:{}", properties.getClient().getGateway().getHost(),
        properties.getClient().getGateway().getPort());

    eClientSocket.eConnect(properties.getClient().getGateway().getHost(),
        properties.getClient().getGateway().getPort(), properties.getClient().getGateway().getId());

    EReader reader = new EReader(eClientSocket, signal);
    reader.start();

    log.info("Connected to {}:{}", properties.getClient().getGateway().getHost(),
        properties.getClient().getGateway().getPort());

    new Thread(() -> {
      while (eClientSocket.isConnected()) {
        signal.waitForSignal();
        log.info("Waiting for signal");
        try {
          reader.processMsgs();
          log.info("Processing Messages");
        } catch (Exception e) {
          log.error(e.getMessage(), e);
        }
      }
    }).start();
  }

  public static void startAPI() {
    if (eClientSocket.isAsyncEConnect()) {
      eClientSocket.startAPI();
    }
  }

  public void disconnect() {
    eClientSocket.eDisconnect();
  }

  public EClientSocket getEClientSocket() {
    if (!eClientSocket.isConnected()) {
      disconnect();
      connect();
    }

    return eClientSocket;
  }

  public static synchronized int getNextOrderId() {
    return orderIds.getAndIncrement();
  }

  public static synchronized void setNextOrderId(int ibOrderId) {
    int maxDBOrderId = orderService.findMaxBrokerOrderId();
    if (ibOrderId >= maxDBOrderId) {
      orderIds.set(ibOrderId);
    } else {
      orderIds.set(++maxDBOrderId);
    }
  }

  public void placeOrder(Contract contract, Order order) {
    String message = "";
    try {
      message = String.format(
          "Placing order. Id: %d, parentId: %d, symbol: %s, action: %s, orderType: %s," +
              " quantity: %d, limitPrice: %s, auxPrice: %s, tif: %s, transmit: %b",
          order.orderId(), order.parentId(), contract.symbol(), order.action().name(),
          order.getOrderType(), order.totalQuantity().longValue(),
          Util.DoubleMaxString(order.lmtPrice()), Util.DoubleMaxString(order.auxPrice()),
          order.tif().name(), order.transmit());

      log.info(message);
      notificationService.notify(message);
      eClientSocket.placeOrder(order.orderId(), contract, order);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      notificationService.notify(message + " --> " + e.getMessage());
    }
  }

  public void requestPositions() {
    log.info("Requesting positions");
    eClientSocket.reqPositions();
  }

  public void getContractDetails(int id, Contract contract) {
    eClientSocket.reqContractDetails(id, contract);
  }

  public void getMarketData(Contract contract) {
    eClientSocket.reqMarketDataType(1);
    eClientSocket.reqMktData(current().nextInt(), contract, "", false, false,
        null);
  }

  public void cancelOrder(int orderId, OrderCancel orderCancel) {
    eClientSocket.cancelOrder(orderId, orderCancel);
  }

  public void requestSinglePnl(int requestId, String accountId, String s, int conId) {
    log.info("Requesting single PNL. requestId: {}, accountId: {}, conId: {}", requestId, accountId,
        conId);

    eClientSocket.reqPnLSingle(requestId, accountId, s, conId);
  }

  public void requestOpenOrders() {
    eClientSocket.reqOpenOrders();
  }
}
