package com.quant.finance.execution.client;

import static java.util.concurrent.ThreadLocalRandom.current;

import com.ib.client.Contract;
import com.ib.client.EClientSocket;
import com.ib.client.EJavaSignal;
import com.ib.client.EReader;
import com.ib.client.Order;
import com.ib.client.OrderCancel;
import com.ib.client.Util;
import com.ib.controller.AccountSummaryTag;
import com.quant.finance.execution.config.ApplicationProperties;
import com.quant.finance.execution.service.EWrapperImpl;
import com.quant.finance.execution.service.NotificationService;
import com.quant.finance.execution.service.OrderService;
import com.quant.finance.execution.util.EngineUtil;
import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class IBClient {
  private static final AtomicInteger orderIds = new AtomicInteger(current()
      .nextInt(1, Integer.MAX_VALUE));

  private static final List<Integer> accountSummaryRequestIds = new CopyOnWriteArrayList<>();

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
        //log.info("Waiting for signal");
        try {
          reader.processMsgs();
          //log.info("Processing Messages");
        } catch (Exception e) {
          log.error(e.getMessage(), e);
        }
      }
    }).start();

    //todo
    //startAccountUpdates();
  }

  public static void startAPI() {
    if (eClientSocket.isAsyncEConnect()) {
      eClientSocket.startAPI();
    }
  }

  public void reconnect() {
    eClientSocket.eDisconnect();
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      log.error(e.getMessage(), e);
    }
    connect();
  }


  public void reconnectWithSleep() {
    eClientSocket.eDisconnect();
    try {
      Thread.sleep(60000);
    } catch (InterruptedException e) {
      log.error(e.getMessage(), e);
    }
    connect();
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
    Integer maxDBOrderId = orderService.findMaxBrokerOrderId();
    if (maxDBOrderId != null && maxDBOrderId > ibOrderId) {
      orderIds.set(++maxDBOrderId);
    } else {
      orderIds.set(ibOrderId);
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
    log.info("Getting contract details");
    eClientSocket.reqContractDetails(id, contract);
  }

  public void getMarketData(Contract contract) {
    log.info("Requesting market data");
    eClientSocket.reqMarketDataType(1);
    eClientSocket.reqMktData(current().nextInt(), contract, "", false, false,
        null);
  }

  public void cancelOrder(int orderId, OrderCancel orderCancel) {
    log.info("Cancel order with orderId: {}", orderId);
    eClientSocket.cancelOrder(orderId, orderCancel);
  }

  public void requestPnl() {
    int requestId = EngineUtil.nextRequestId();
    String accountId = properties.getAccount().getId();

    log.info("Requesting pnl. requestId: {}", requestId);
    eClientSocket.reqPnL(requestId, accountId, "");
  }

  public void requestSinglePnl(int requestId, String accountId, String modelCode, int conId) {
    log.info("Requesting single PNL. requestId: {}, accountId: {}, modelCode: {},conId: {}",
        requestId, accountId, modelCode, conId);
    eClientSocket.reqPnLSingle(requestId, accountId, modelCode, conId);
  }

  public void requestSinglePnl(int conId) {
    int requestId = EngineUtil.nextRequestId();
    String accountId = properties.getAccount().getId();

    log.info("Requesting single PNL. requestId: {}, accountId: {}, conId: {}", requestId, accountId,
        conId);

    eClientSocket.reqPnLSingle(requestId, accountId, "", conId);
  }

  public void requestOpenOrders() {
    log.info("Requesting open orders");
    eClientSocket.reqOpenOrders();
  }

  public void requestAccountUpdates() {
    log.info("Requesting Account Updates");
    eClientSocket.reqAccountUpdates(true, properties.getAccount().getId());
  }

  public void cancelAccountUpdates() {
    log.info("Cancelling Account Updates");
    eClientSocket.reqAccountUpdates(true, properties.getAccount().getId());
  }

  public void requestAccountSummary() {
    log.info("Requesting Account Summary");

    if (!accountSummaryRequestIds.isEmpty()) {
      log.warn("There is existing Account Summary Request");
      return;
    }

    int requestId = EngineUtil.nextRequestId();
    accountSummaryRequestIds.add(requestId);

    eClientSocket.reqAccountSummary(requestId, "All",
        neededAccountSummaryTags());
  }

  public void cancelAccountSummary() {
    log.info("Cancelling Account Summary");

    accountSummaryRequestIds.forEach(eClientSocket::cancelAccountSummary);
    accountSummaryRequestIds.clear();
  }

  private String neededAccountSummaryTags() {
    StringBuilder builder = new StringBuilder();

    //builder.append(AccountSummaryTag.AccountType.name());
    //builder.append(", " + AccountSummaryTag.AvailableFunds.name());
    //builder.append(", " + AccountSummaryTag.NetLiquidation.name());
    //builder.append(", " + AccountSummaryTag.TotalCashValue.name());
    //builder.append(", " + AccountSummaryTag.BuyingPower.name());

    List<String> tagList = neededAccountSummaryTagList();

    for (int i = 0; i < tagList.size(); i++) {
      String tag = tagList.get(i);

      if (i == 0) {
        builder.append(tag);
      } else {
        builder.append("," + tag);
      }
    }

    return builder.toString();
  }

  public static List<String> neededAccountSummaryTagList() {
    return Arrays.asList(
        AccountSummaryTag.AvailableFunds.name(),
        AccountSummaryTag.NetLiquidation.name(),
        AccountSummaryTag.TotalCashValue.name(),
        AccountSummaryTag.BuyingPower.name()
    );
  }
}
