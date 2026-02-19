package com.quant.finance.execution.service;

import com.ib.client.CommissionAndFeesReport;
import com.ib.client.Contract;
import com.ib.client.Decimal;
import com.ib.client.Execution;
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

  public OrderService(OrderRepository repository, NotificationService notificationService,
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
    return OrderEntity.builder()
        .brokerOrderId(String.valueOf(IBClient.getNextOrderId()))
        .alertId(alert.getId())
        .strategyId(strategy.getId())
        .symbol(alert.getSymbol())
        .contractId(contract.conid())
        .action(alert.getAction())
        .quantity(strategy.getMaxPositionQuantity())
        .currency(alert.getQuote())
        .status(OrderStatus.ApiPending)
        .orderType(OrderType.MKT)
        .limitPrice(calculateLimitPrice())
        .stopPrice(calculateStopPrice()).build();
  }

  private BigDecimal calculateLimitPrice() {
    return null;
  }

  private BigDecimal calculateStopPrice() {
    return null;
  }

  public void commissionAndFeesReport(CommissionAndFeesReport report) {
    log.info("COMMISSION AND FEES REPORT DETAILS. ExecutionId: {}, commissionAndFees: {}," +
            " currency: {}, yield: {}, yieldRedemptionDate: {}", report.execId(),
        report.commissionAndFees(), report.currency(), report.yield(),
        report.yieldRedemptionDate());

    Optional<OrderEntity> optionalOrder =
        repository.findByExecutionId(String.valueOf(report.execId()));

    if (optionalOrder.isPresent()) {
      optionalOrder.get().setCommission(BigDecimal.valueOf(report.commissionAndFees()));
      repository.save(optionalOrder.get());
    } else {
      String message = String.format("Order not found with executionId: %s", report.execId());
      log.error(message);
      notificationService.notify(message);
    }
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

    Optional<OrderEntity> optionalOrder = repository.findByBrokerOrderId(String.valueOf(orderId));

    if (optionalOrder.isPresent()) {
      OrderEntity order = optionalOrder.get();
      order.setStatus(OrderStatus.get(status));
      order.setFilledQuantity(filled.value().doubleValue());
      order.setAverageFillPrice(BigDecimal.valueOf(avgFillPrice));

      if (OrderStatus.get(status) == OrderStatus.PreSubmitted ||
          OrderStatus.get(status) == OrderStatus.Submitted) {
        optionalOrder.get().setSubmittedAt(LocalDateTime.now());
      } else if (OrderStatus.get(status) == OrderStatus.Filled) {
        optionalOrder.get().setFilledAt(LocalDateTime.now());
      }

      repository.save(optionalOrder.get());
    } else {
      message = String.format("Order not found with id: %d", orderId);
      log.error(message);
      notificationService.notify(message);
    }
  }

  public void execDetails(int i, Contract contract, Execution execution) {
    log.info("EXECUTION DETAILS. OrderId: {}, Price: {}, Shares: {}", execution.orderId(),
        execution.price(), execution.shares());

    Optional<OrderEntity> optionalOrder =
        repository.findByBrokerOrderId(String.valueOf(execution.orderId()));

    if (optionalOrder.isPresent()) {
      optionalOrder.get().setExecutionId(execution.execId());
      repository.save(optionalOrder.get());
    } else {
      String message = String.format("Order not found with id: %d", execution.orderId());
      log.error(message);
      notificationService.notify(message);
    }
  }
}
