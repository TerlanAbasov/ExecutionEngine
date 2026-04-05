package com.quant.finance.execution.schedule;

import com.quant.finance.execution.client.IBClient;
import com.quant.finance.execution.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConnectionScheduler {
  private final IBClient ibClient;
  private final NotificationService notificationService;

  @Scheduled(fixedDelayString = "${application.params.twsConnectionCheckerFixedDelay}")
  public void checkConnectivity() {
    try {
      Thread.sleep(10000);
      if (!ibClient.isConnected()) {
        log.error("Engine is not connected to TWS API.");
        notificationService.notify("Engine is not connected to TWS API.");
      }
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }

}
