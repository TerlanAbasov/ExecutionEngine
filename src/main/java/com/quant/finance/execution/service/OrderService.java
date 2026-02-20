package com.quant.finance.execution.service;

import com.ib.client.Contract;
import com.ib.client.Decimal;
import com.ib.client.OrderStatus;
import com.ib.client.OrderType;
import com.quant.finance.execution.client.IBClient;
import com.quant.finance.execution.entity.AlertEntity;
import com.quant.finance.execution.entity.OrderEntity;
import com.quant.finance.execution.entity.StrategyEntity;
import com.quant.finance.execution.repository.OrderRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class OrderService {
  private final OrderRepository repository;
  private final NotificationService notificationService;
  private final EWrapperImpl eWrapper;

  public OrderService(OrderRepository repository,
                      NotificationService notificationService,
                      @Lazy EWrapperImpl eWrapper) {
    this.repository = repository;
    this.notificationService = notificationService;
    this.eWrapper = eWrapper;
  }

  @Transactional
  public OrderEntity save(OrderEntity order) {
    return repository.save(order);
  }

  @Transactional(readOnly = true)
  public Optional<OrderEntity> findByBrokerOrderId(String brokerOrderId) {
    return repository.findByBrokerOrderId(brokerOrderId);
  }

  @Transactional(readOnly = true)
  public Optional<OrderEntity> findByExecutionId(String executionId) {
    return repository.findByBrokerOrderId(executionId);
  }

  public OrderEntity createOrderEntity(AlertEntity alert, StrategyEntity strategy,
                                       Contract contract) {
    //todo get SELL quantity from BUY order

    //ExecutionEntity execution = ExecutionEntity.builder()
    //    .currency(alert.getQuote())
    //    .quantity(strategy.getMaxPositionQuantity())
    //    .build();

    //todo remove exection

    OrderEntity orderEntity = OrderEntity.builder()
        .brokerOrderId(String.valueOf(IBClient.getNextOrderId()))
        .strategy(strategy)
        .symbol(alert.getSymbol())
        .contractId(contract.conid())
        .action(alert.getAction())
        .quantity(strategy.getMaxPositionQuantity())
        .status(OrderStatus.ApiPending)
        .orderType(OrderType.MKT)
        .limitPrice(calculateLimitPrice())
        .stopPrice(calculateStopPrice())
        .build();

    //alert.addOrder(orderEntity);
    orderEntity.setAlert(alert);
    orderEntity = repository.save(orderEntity);

    return orderEntity;
  }

  private BigDecimal calculateLimitPrice() {
    return null;
  }

  private BigDecimal calculateStopPrice() {
    return null;
  }

  public void orderStatus(int orderId, String status, Decimal filled, Decimal remaining,
                          double avgFillPrice, long l, int i1, double v1, int i2, String s1,
                          double v2) {
    String message = String.format(
        "ORDER STATUS. orderId: %d, status: %s, filled: %d, remaining: %d, avgPrice: %f", orderId,
        status, filled.longValue(), remaining.longValue(), avgFillPrice);

    log.info(message);

    if (status.equals(OrderStatus.Filled.name())) {
      eWrapper.getEClientSocket().reqPositions();
      notificationService.notify(message);
    }

    OrderEntity order = repository.findByBrokerOrderId(String.valueOf(orderId))
        .orElse(null);

    if (order == null) {
      String errorMessage = String.format("Order not found with id: %d", orderId);
      log.error(errorMessage);
      notificationService.notify(errorMessage);

      return;
    }

    order.setStatus(OrderStatus.get(status));
    //order.getExecution().setPrice(BigDecimal.valueOf(avgFillPrice));

    if (order.getQuantity() != filled.value().doubleValue()) {
      log.error("Order Quantity Mismatch. Ordered quantity: {}, filled quantity: {}",
          order.getQuantity(), filled.value().doubleValue());
    }

    setDateTimes(order, status);

    repository.save(order);
  }

  private void setDateTimes(OrderEntity order, String status) {
    if (OrderStatus.get(status) == OrderStatus.PreSubmitted ||
        OrderStatus.get(status) == OrderStatus.Submitted) {
      order.setSubmittedAt(LocalDateTime.now());
    } else if (OrderStatus.get(status) == OrderStatus.Filled) {
      order.setFilledAt(LocalDateTime.now());
    }
  }

}
