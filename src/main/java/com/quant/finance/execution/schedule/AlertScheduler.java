package com.quant.finance.execution.schedule;

import static org.springframework.data.domain.PageRequest.of;

import com.quant.finance.execution.entity.AlertEntity;
import com.quant.finance.execution.enums.AlertState;
import com.quant.finance.execution.repository.AlertRepository;
import com.quant.finance.execution.service.AlertService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertScheduler {
  private final AlertRepository alertRepository;
  private final AlertService alertService;

  @Scheduled(fixedDelayString = "${application.params.alertProcessorFixedDelay}")
  public void processAlerts() {
    List<AlertEntity> alerts = alertRepository.findNextAlerts(of(0, 10));

    for (AlertEntity alert : alerts) {
      try {
        alertService.processAlert(alert);
      } catch (Exception e) {
        log.error(e.getMessage(), e);
        alertService.updateStateAndDescription(alert, AlertState.FAILED, e.getMessage());
      }
    }
  }
}
