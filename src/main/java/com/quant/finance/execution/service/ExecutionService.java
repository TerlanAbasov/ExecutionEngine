package com.quant.finance.execution.service;

import com.ib.client.CommissionAndFeesReport;
import com.ib.client.Contract;
import com.ib.client.Execution;
import com.quant.finance.execution.entity.ExecutionEntity;
import com.quant.finance.execution.entity.OrderEntity;
import com.quant.finance.execution.repository.ExecutionRepository;
import com.quant.finance.execution.repository.OrderRepository;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExecutionService {

  private final NotificationService notificationService;
  private final ExecutionRepository executionRepository;
  private final OrderRepository orderRepository;

  public void execDetails(int id, Contract contract, Execution execution) {
    log.info("EXECUTION DETAILS. OrderId: {}, Price: {}, Shares: {}", execution.orderId(),
        execution.price(), execution.shares());

    OrderEntity order =
        orderRepository.findByBrokerOrderId(String.valueOf(execution.orderId()))
            .orElse(null);

    if (order == null) {
      String errorMessage = String.format("Order not found with id: %d", execution.orderId());
      log.error(errorMessage);
      notificationService.notify(errorMessage);

      return;
    }

    ExecutionEntity executionEntity = ExecutionEntity.builder()
        .execId(execution.execId())
        .price(BigDecimal.valueOf(execution.price()))
        .filledQuantity(execution.shares().value().doubleValue())
        .currency(contract.currency())
        .build();

    executionEntity.setOrder(order);

    try {
      executionRepository.save(executionEntity);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }

    //order.addExecution(executionEntity);
    //orderRepository.save(order);
  }

  public void commissionAndFeesReport(CommissionAndFeesReport report) {
    log.info("COMMISSION AND FEES REPORT DETAILS. ExecutionId: {}, commissionAndFees: {}," +
            " currency: {}, realizedPNL: {}, yield: {}, yieldRedemptionDate: {}", report.execId(),
        report.commissionAndFees(), report.currency(), report.realizedPNL(), report.yield(),
        report.yieldRedemptionDate());

    notificationService.notify(String.format("ExecutionId: %s, realizedPNL: %s", report.execId(),
        report.realizedPNL()));

    ExecutionEntity executionEntity =
        executionRepository.findByExecId(String.valueOf(report.execId()))
            .orElse(null);

    if (executionEntity == null) {
      String errorMessage = String.format("Execution not found with execId: %s", report.execId());
      log.error(errorMessage);
      notificationService.notify(errorMessage);

      return;
    }

    executionEntity.setCommission(BigDecimal.valueOf(report.commissionAndFees()));
    executionEntity.setRealizedPnl(BigDecimal.valueOf(report.realizedPNL()));
    executionEntity.setTotalAmount(calculateTotalAmount(executionEntity));

    executionRepository.save(executionEntity);
  }

  private BigDecimal calculateTotalAmount(ExecutionEntity execution) {
    BigDecimal amount = BigDecimal.ZERO;

    if (execution.getPrice() != null && execution.getFilledQuantity() != null) {
      amount =
          execution.getPrice().multiply(BigDecimal.valueOf(execution.getFilledQuantity()));
    }

    amount = execution.getCommission() != null ? amount.add(execution.getCommission()) :
        amount;

    return amount;
  }

  public void makeMainOrder() {
  }

  public void makeStopOrder() {
  }

  public void makeTakeProfitOrder() {
    // TODO: 09.02.26 need analysis
  }

  private BigDecimal calculateLimitPrice() {
    return null;
  }

  private BigDecimal calculateStopPrice() {
    return null;
  }
}
