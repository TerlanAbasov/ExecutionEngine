package com.quant.finance.execution.service;

import com.ib.client.CommissionAndFeesReport;
import com.ib.client.Contract;
import com.ib.client.Decimal;
import com.ib.client.Execution;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StartupRunner implements CommandLineRunner {
  private final NotificationService notificationService;
  private final ExecutionService executionService;

  @Override
  public void run(String... args) throws Exception {
    notificationService.notify("\uD83D\uDE80 ExecutionEngine is running!");

    //simulate();
  }

  private void simulate() throws InterruptedException {
    Thread.sleep(10000);
    Contract contract = new Contract();
    contract.currency("USD");

    Execution execution = new Execution();
    execution.orderId(25);
    execution.execId("exec3");
    execution.price(26.30);
    execution.shares(Decimal.get(3));

    executionService.execDetails(1, contract, execution);

    Thread.sleep(10000);

    CommissionAndFeesReport report = new CommissionAndFeesReport();
    report.execId("exec3");
    report.realizedPNL(1.2);
    report.commissionAndFees(1);
    report.yield(1);
    report.yieldRedemptionDate(1);
    report.currency("USD");

    executionService.commissionAndFeesReport(report);
  }
}
