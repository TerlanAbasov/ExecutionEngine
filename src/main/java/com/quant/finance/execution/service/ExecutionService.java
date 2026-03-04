package com.quant.finance.execution.service;

import static com.ib.client.Util.DoubleMaxString;

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
        orderRepository.findByBrokerOrderId(execution.orderId())
            .orElse(null);

    if (order == null) {
      String errorMessage = String.format("Order not found with id: %d", execution.orderId());
      log.error(errorMessage);
      notificationService.notify(errorMessage);

      return;
    }

    ExecutionEntity executionEntity = ExecutionEntity.builder()
        .execId(execution.execId())
        .price(new BigDecimal(DoubleMaxString(execution.price(), "0")))
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
        DoubleMaxString(report.commissionAndFees()), report.currency(),
        DoubleMaxString(report.realizedPNL()),
        DoubleMaxString(report.yield()),
        report.yieldRedemptionDate());

    notificationService.notify(String.format("ExecutionId: %s, realizedPNL: %s", report.execId(),
        DoubleMaxString(report.realizedPNL())));

    ExecutionEntity executionEntity =
        executionRepository.findByExecId(String.valueOf(report.execId()))
            .orElse(null);

    if (executionEntity == null) {
      String errorMessage = String.format("Execution not found with execId: %s", report.execId());
      log.error(errorMessage);
      notificationService.notify(errorMessage);

      return;
    }

    executionEntity.setCommission(
        new BigDecimal(DoubleMaxString(report.commissionAndFees(), "0")));
    executionEntity.setRealizedPnl(new BigDecimal(DoubleMaxString(report.realizedPNL(), "0")));
    executionEntity.setTotalAmount(calculateTotalAmount(executionEntity));

    executionRepository.save(executionEntity);
  }

  private boolean isValidDoubleValue(double value) {
    return Double.isFinite(value) && value != Double.MAX_VALUE && value != Double.MIN_VALUE;
  }

  private BigDecimal calculateTotalAmount(ExecutionEntity execution) {
    BigDecimal amount = BigDecimal.ZERO;

    if (execution.getPrice() != null && execution.getFilledQuantity() != null) {
      amount =
          execution.getPrice().multiply(BigDecimal.valueOf(execution.getFilledQuantity()));
    }

    if (execution.getCommission() != null) {
      amount.add(execution.getCommission());
    }

    return amount;
  }

}
