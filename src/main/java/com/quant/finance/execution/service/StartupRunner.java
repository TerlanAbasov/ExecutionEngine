package com.quant.finance.execution.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StartupRunner implements CommandLineRunner {
  private final NotificationService notificationService;

  @Override
  public void run(String... args) throws Exception {
    notificationService.notify("ExecutionEngine is running!");
  }
}
