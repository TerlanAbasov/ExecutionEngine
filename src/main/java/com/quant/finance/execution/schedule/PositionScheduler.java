package com.quant.finance.execution.schedule;

import com.quant.finance.execution.client.IBClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PositionScheduler {

  private final IBClient ibClient;

  @Scheduled(fixedDelayString = "${application.params.positionSyncronizerDelay}")
  public void syncronizePositions() {
    try {
      if (ibClient.isConnected()) {
        log.info("Syncronizing positions");
        ibClient.requestPositions();
      }
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }
}
