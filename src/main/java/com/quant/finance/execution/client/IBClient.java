package com.quant.finance.execution.client;

import com.ib.client.Contract;
import com.ib.client.Order;
import com.quant.finance.execution.service.EWrapperImpl;
import com.quant.finance.execution.service.NotificationService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class IBClient {
  private static final AtomicInteger orderIds =
      new AtomicInteger(ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE));
  private final EWrapperImpl eWrapper;
  private final NotificationService notificationService;

  public static synchronized int getNextOrderId() {
    return orderIds.getAndIncrement();
  }

  public static synchronized void setNextOrderId(int id) {
    orderIds.set(id);
  }

  public void placeOrder(Contract contract, Order order) {
    String message = String.format("Placing order. Symbol: %s, action: %s, quantity: %d, id: %d",
        contract.symbol(),
        order.action().name(), order.totalQuantity().longValue(), order.orderId());
    log.info(message);
    notificationService.notify(message);
    eWrapper.getEClientSocket().placeOrder(order.orderId(), contract, order);
  }

  public void getContractDetails(int id, Contract contract) {
    eWrapper.getEClientSocket().reqContractDetails(id, contract);
  }

  public void getMarketData(Contract contract) {
    eWrapper.getEClientSocket().reqMarketDataType(1);
    eWrapper.getEClientSocket()
        .reqMktData(ThreadLocalRandom.current().nextInt(), contract, "", false, false, null);
  }
}
