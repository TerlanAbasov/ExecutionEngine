package com.quant.finance.execution.service;

import static com.ib.client.OrderStatus.ApiPending;
import static com.ib.client.OrderStatus.PendingSubmit;
import static com.ib.client.OrderStatus.PreSubmitted;
import static com.ib.client.OrderStatus.Submitted;

import com.ib.client.ContractDetails;
import com.ib.client.Decimal;
import com.ib.client.OrderCancel;
import com.ib.client.OrderStatus;
import com.ib.client.OrderType;
import com.ib.client.Types.Action;
import com.quant.finance.execution.client.IBClient;
import com.quant.finance.execution.entity.AlertEntity;
import com.quant.finance.execution.entity.OrderEntity;
import com.quant.finance.execution.entity.StrategyEntity;
import com.quant.finance.execution.repository.OrderRepository;
import com.quant.finance.execution.util.EngineUtil;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
  private final OrderRepository repository;
  private final NotificationService notificationService;
  @Lazy
  @Autowired
  private IBClient ibClient;

  @Transactional
  public OrderEntity save(OrderEntity order) {
    return repository.save(order);
  }

  @Transactional(readOnly = true)
  public Optional<OrderEntity> findByBrokerOrderId(int brokerOrderId) {
    return repository.findByBrokerOrderId(brokerOrderId);
  }

  @Transactional(readOnly = true)
  public List<OrderEntity> findBySymbol(String symbol) {
    return repository.findBySymbol(symbol);
  }

  @Transactional(readOnly = true)
  public List<OrderEntity> findCancellableOrdersBySymbol(String symbol) {
    return repository.findBySymbolAndStatusIn(symbol, List.of(ApiPending, PreSubmitted, Submitted,
        PendingSubmit));
  }

  @Transactional(readOnly = true)
  public List<OrderEntity> findCancellableOrders() {
    return repository.findByStatusIn(List.of(ApiPending, PreSubmitted, Submitted,
        PendingSubmit));
  }

  @Transactional(readOnly = true)
  public Optional<OrderEntity> findByExecutionId(int executionId) {
    return repository.findByBrokerOrderId(executionId);
  }

  @Transactional(readOnly = true)
  public Integer findMaxBrokerOrderId() {
    return repository.findMaxBrokerOrderId();
  }

  public OrderEntity buildAndSaveParentOrder(AlertEntity alert, StrategyEntity strategy,
                                             ContractDetails contractDetails,
                                             double existingQuantity) {
    OrderEntity order = OrderEntity.builder()
        .brokerOrderId(IBClient.getNextOrderId())
        .strategy(strategy)
        .symbol(alert.getSymbol())
        .contractId(contractDetails.contract().conid())
        .action(alert.getAction())
        .status(ApiPending)
        .build();

    order.setAlert(alert);

    if (alert.getAction() == Action.BUY) {
      order.setOrderType(strategy.getBuyOrderType());
      if (strategy.getBuyOrderType() == OrderType.LMT) {
        order.setLimitPrice(alert.getClose().multiply(strategy.getBuyLimitCeiling()));
      }
      order.setQuantity(calculateQuantity(strategy, alert));
      order.setTakeProfitPrice(calculateTakeProfitPrice(strategy, alert));
      order.setStopLossPrice(calculateStopLossPrice(strategy, alert));
    } else if (alert.getAction() == Action.SELL) {
      order.setOrderType(strategy.getSellOrderType());
      order.setQuantity(existingQuantity);

      if (strategy.getSellOrderType() == OrderType.LMT) {
        order.setLimitPrice(alert.getOpen().multiply(strategy.getSellLimitFloor()));
      }
    }

    order = repository.save(order);

    return order;
  }

  public OrderEntity buildAndSaveChildOrder(OrderEntity parentOrder, OrderType orderType,
                                            Action action) {
    OrderEntity orderEntity = OrderEntity.builder()
        .brokerOrderId(IBClient.getNextOrderId())
        .alert(parentOrder.getAlert())
        .strategy(parentOrder.getStrategy())
        .symbol(parentOrder.getSymbol())
        .contractId(parentOrder.getContractId())
        .action(action)
        .quantity(parentOrder.getQuantity())
        .status(ApiPending)
        .orderType(orderType)
        .parentOrderId(parentOrder.getId())
        .build();

    if (orderType == OrderType.LMT) {
      orderEntity.setTakeProfitPrice(parentOrder.getTakeProfitPrice());
    } else if (orderType == OrderType.STP) {
      orderEntity.setStopLossPrice(parentOrder.getStopLossPrice());
    }

    return repository.save(orderEntity);
  }

  private double calculateQuantity(StrategyEntity strategy, AlertEntity alert) {
    double quantity = strategy.getMaxPositionAmount().doubleValue() / alert.getHigh().doubleValue();

    return Math.floor(quantity);
  }

  private BigDecimal calculateTakeProfitPrice(StrategyEntity strategy,
                                              AlertEntity alert) {

    BigDecimal percentageValue =
        (alert.getHigh().multiply(BigDecimal.valueOf(strategy.getTakeProfitPercentage())))
            .divide(BigDecimal.valueOf(100));

    BigDecimal limitPrice = alert.getHigh().add(percentageValue);

    //todo optimize with ATR
    return limitPrice;
  }

  private BigDecimal calculateStopLossPrice(StrategyEntity strategy,
                                            AlertEntity alert) {
    BigDecimal percentageValue =
        (alert.getLow().multiply(BigDecimal.valueOf(strategy.getStopLossPercentage())))
            .divide(BigDecimal.valueOf(100));

    BigDecimal stopPrice = alert.getLow().subtract(percentageValue);

    //todo optimize with ATR
    return stopPrice;
  }

  public void orderStatus(int orderId, String status, Decimal filled, Decimal remaining,
                          double avgFillPrice, long l, int i1, double v1, int i2, String s1,
                          double v2) {
    String message = String.format(
        "ORDER STATUS. orderId: %d, status: %s, filled: %d, remaining: %d, avgPrice: %f", orderId,
        status, filled.longValue(), remaining.longValue(), avgFillPrice);

    log.info(message);

    try {
      if (status.equals(OrderStatus.Filled.name())) {
        ibClient.requestPositions();
        notificationService.notify(message);
      }

      Optional<OrderEntity> order = repository.findByBrokerOrderId(orderId);

      if (order.isPresent()) {
        order.get().setStatus(OrderStatus.get(status));
        setDateTimes(order.get(), status);
        repository.save(order.get());
      } else {
        String errorMessage = String.format("Order not found with id: %d", orderId);
        log.error(errorMessage);
        //notificationService.notify(errorMessage);
      }
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      notificationService.notify(e.getMessage());
    }
  }

  private void setDateTimes(OrderEntity order, String status) {
    if (OrderStatus.get(status) == PreSubmitted ||
        OrderStatus.get(status) == Submitted) {
      order.setSubmittedAt(LocalDateTime.now());
    } else if (OrderStatus.get(status) == OrderStatus.Filled) {
      order.setFilledAt(LocalDateTime.now());
    }
  }

  @Transactional
  public void cancelOrderBy(String identifier) {
    if (EngineUtil.isDigit(identifier)) {
      cancelOrderById(Integer.parseInt(identifier));
    } else {
      cancelOrderBySymbol(identifier);
    }
  }

  @Transactional
  private void cancelOrderById(int orderId) {
    Optional<OrderEntity> order = findByBrokerOrderId(orderId);
    if (order.isPresent()) {
      cancelIfIsActive(order.get());
    }
  }

  @Transactional
  private void cancelOrderBySymbol(String symbol) {
    findCancellableOrdersBySymbol(symbol).forEach(this::cancelIfIsActive);
  }

  @Transactional
  public void cancelOpenOrders() {
    findCancellableOrders().forEach(this::cancelIfIsActive);
  }

  private void cancelIfIsActive(OrderEntity order) {
    if (order.getStatus().isActive() || order.getStatus() == OrderStatus.ApiPending) {
      log.info("Cancelling order with id: {}, status: {}", order.getBrokerOrderId(),
          order.getStatus());
      ibClient.cancelOrder(order.getBrokerOrderId(), new OrderCancel());
    } else {
      log.info("Unable to cancel order with id: {}, status: {}", order.getBrokerOrderId(),
          order.getStatus());
    }
  }

}
