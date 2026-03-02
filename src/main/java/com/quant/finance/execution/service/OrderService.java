package com.quant.finance.execution.service;

import static com.ib.client.OrderStatus.ApiPending;
import static com.ib.client.OrderStatus.PendingSubmit;
import static com.ib.client.OrderStatus.PreSubmitted;
import static com.ib.client.OrderStatus.Submitted;

import com.ib.client.Contract;
import com.ib.client.Decimal;
import com.ib.client.OrderStatus;
import com.ib.client.OrderType;
import com.ib.client.Types.Action;
import com.quant.finance.execution.client.IBClient;
import com.quant.finance.execution.entity.AlertEntity;
import com.quant.finance.execution.entity.OrderEntity;
import com.quant.finance.execution.entity.StrategyEntity;
import com.quant.finance.execution.repository.OrderRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
  public Optional<OrderEntity> findByBrokerOrderId(String brokerOrderId) {
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
  public Optional<OrderEntity> findByExecutionId(String executionId) {
    return repository.findByBrokerOrderId(executionId);
  }

  public OrderEntity buildAndSaveParentOrder(AlertEntity alert, StrategyEntity strategy,
                                             Contract contract, double existingQuantity) {
    OrderEntity order = OrderEntity.builder()
        .brokerOrderId(String.valueOf(IBClient.getNextOrderId()))
        .strategy(strategy)
        .symbol(alert.getSymbol())
        .contractId(contract.conid())
        .action(alert.getAction())
        .status(ApiPending)
        .build();

    order.setAlert(alert);

    if (alert.getAction() == Action.BUY) {
      order.setOrderType(strategy.getBuyOrderType());
      if (strategy.getBuyOrderType() == OrderType.LMT) {
        //setParentLimitPrice(order, alert);
        order.setLimitPrice(alert.getClose().multiply(strategy.getBuyLimitCeiling()));
      }
      order.setQuantity(calculateQuantity(strategy, alert));
      order.setTakeProfitPrice(calculateTakeProfitPrice(strategy, alert));
      order.setStopLossPrice(calculateStopLossPrice(strategy, alert));
    } else if (alert.getAction() == Action.SELL) {
      order.setOrderType(strategy.getSellOrderType());
      if (strategy.getSellOrderType() == OrderType.LMT) {
        order.setLimitPrice(alert.getOpen().multiply(strategy.getSellLimitFloor()));
      }
      order.setQuantity(existingQuantity);
    }

    order = repository.save(order);

    return order;
  }

  private void setParentLimitPrice(OrderEntity order, AlertEntity alert) {
    //todo
    BigDecimal price =
        (alert.getOpen().add(alert.getClose())).divide(BigDecimal.TWO, RoundingMode.CEILING);

    order.setLimitPrice(price);
  }

  public OrderEntity buildAndSaveChildOrder(OrderEntity mainOrder, OrderType orderType,
                                            Action action) {
    OrderEntity orderEntity = OrderEntity.builder()
        .brokerOrderId(String.valueOf(IBClient.getNextOrderId()))
        .alert(mainOrder.getAlert())
        .strategy(mainOrder.getStrategy())
        .symbol(mainOrder.getSymbol())
        .contractId(mainOrder.getContractId())
        .action(action)
        .quantity(mainOrder.getQuantity())
        .status(ApiPending)
        .orderType(orderType)
        .parentOrderId(mainOrder.getId())
        .build();

    if (orderType == OrderType.LMT) {
      orderEntity.setTakeProfitPrice(mainOrder.getTakeProfitPrice());
    } else if (orderType == OrderType.STP) {
      orderEntity.setStopLossPrice(mainOrder.getStopLossPrice());
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

      OrderEntity order = repository.findByBrokerOrderId(String.valueOf(orderId))
          .orElse(null);

      if (order != null) {
        order.setStatus(OrderStatus.get(status));
        setDateTimes(order, status);
        repository.save(order);
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

}
