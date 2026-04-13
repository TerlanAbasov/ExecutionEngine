package com.quant.finance.execution.service;

import com.ib.client.CommissionAndFeesReport;
import com.ib.client.Contract;
import com.ib.client.Decimal;
import com.ib.client.Execution;
import com.quant.finance.execution.command.CommandDispatcher;
import com.quant.finance.execution.command.CommandParser;
import com.quant.finance.execution.dto.TradeCommandDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

@Service
@RequiredArgsConstructor
@Slf4j
public class StartupRunner implements CommandLineRunner {
  private final NotificationService notificationService;
  private final ExecutionService executionService;
  private final CommandParser commandParser;
  private final CommandDispatcher commandDispatcher;

  @Override
  public void run(String... args) throws Exception {
    notificationService.notify("\uD83D\uDE80 ExecutionEngine is running!");

    //simulateExecution();
    //simulateTelegramCommand();
  }

  private void simulateTelegramCommand() throws InterruptedException {
    Thread.sleep(5000);

    Update update = new Update();
    Message message = new Message();
    message.setText("/restartengine");
    update.setMessage(message);

    consume(update);

  }

  public void consume(Update update) {
    if (!update.hasMessage() || !update.getMessage().hasText()) {
      return;
    }

    Long chatId = 1L;//update.getMessage().getChatId();
    String text = update.getMessage().getText();
    log.info("\uD83D\uDCE9 '{}' command received from chatId={}", text, chatId);

    TradeCommandDto cmd = commandParser.parse(text, chatId);
    String response = commandDispatcher.dispatch(cmd);

    log.info(response);
  }

  private void simulateExecution() throws InterruptedException {
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
