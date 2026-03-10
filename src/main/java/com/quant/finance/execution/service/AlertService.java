package com.quant.finance.execution.service;

import com.ib.client.Types;
import com.quant.finance.execution.config.ApplicationProperties;
import com.quant.finance.execution.dto.TVAlertDto;
import com.quant.finance.execution.entity.AlertEntity;
import com.quant.finance.execution.mapper.AlertMapper;
import com.quant.finance.execution.repository.AlertRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class AlertService {
  private final AlertRepository repository;
  private final AlertMapper alertMapper;
  private final StrategyService strategyService;
  private final NotificationService notificationService;
  private final ApplicationProperties properties;
  private final AlertRoutingService routingService;

  public AlertService(AlertRepository repository,
                      AlertMapper alertMapper,
                      NotificationService notificationService,
                      @Lazy StrategyService strategyService,
                      ApplicationProperties properties,
                      AlertRoutingService routingService) {
    this.repository = repository;
    this.alertMapper = alertMapper;
    this.notificationService = notificationService;
    this.strategyService = strategyService;
    this.properties = properties;
    this.routingService = routingService;
  }

  @Async
  public void processAlert(TVAlertDto tvAlertDto) {
    log.info("Processing alert {}", tvAlertDto);
    notificationService.notify(tvAlertDto);
    routingService.routeToPartner(tvAlertDto);

    AlertEntity alert = repository.save(alertMapper.toEntity(tvAlertDto));

    if (alert.getPeerSymbol() == null || alert.getPeerSymbol().isBlank()) {
      try {
        strategyService.executeStrategy(alert);
      } catch (Exception e) {
        log.error(e.getMessage(), e);
        notificationService.notify(
            "Error while alert processing: " + e.getMessage());
      }

      return;
    }

    if (alert.getAction() == Types.Action.BUY) {
      try {
        AlertEntity peerAlert = alert.duplicateForPeerTickerProcessing();
        strategyService.executeStrategy(peerAlert);
        Thread.sleep(properties.getParams().getPairTickerThreadSleep());
        strategyService.executeStrategy(alert);
      } catch (Exception e) {
        log.error(e.getMessage(), e);
        notificationService.notify(
            "Error while alert processing: " + e.getMessage());
      }
    } else {
      try {
        strategyService.executeStrategy(alert);
        Thread.sleep(properties.getParams().getPairTickerThreadSleep());
        AlertEntity peerAlert = alert.duplicateForPeerTickerProcessing();
        strategyService.executeStrategy(peerAlert);
      } catch (Exception e) {
        log.error(e.getMessage(), e);
        notificationService.notify(
            "Error while alert processing: " + e.getMessage());
      }
    }

  }

  @Transactional
  public AlertEntity save(TVAlertDto tvAlertDto) {
    return repository.save(alertMapper.toEntity(tvAlertDto));
  }
}
