package com.quant.finance.execution.service;

import com.quant.finance.execution.client.RoutingClient;
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
  private final RoutingClient routingClient;
  private final ApplicationProperties properties;

  public AlertService(AlertRepository repository, AlertMapper alertMapper,
                      NotificationService notificationService, RoutingClient routingClient,
                      @Lazy StrategyService strategyService, ApplicationProperties properties) {
    this.repository = repository;
    this.alertMapper = alertMapper;
    this.notificationService = notificationService;
    this.routingClient = routingClient;
    this.strategyService = strategyService;
    this.properties = properties;
  }

  @Async
  public void processAlert(TVAlertDto tvAlertDto) {
    log.info("Processing alert {}", tvAlertDto);
    notificationService.notify(tvAlertDto);
    routeToPartner(tvAlertDto);

    AlertEntity alert = repository.save(alertMapper.toEntity(tvAlertDto));
    AlertEntity peerAlert = alert.duplicateForPeerTickerProcessing();
    try {
      strategyService.executeStrategy(peerAlert);
      Thread.sleep(properties.getParams().getPairTickerThreadSleep());
      strategyService.executeStrategy(alert);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      notificationService.notify(
          "Error while alert processing: " + e.getMessage());
    }
  }

  @Transactional
  public AlertEntity save(TVAlertDto tvAlertDto) {
    return repository.save(alertMapper.toEntity(tvAlertDto));
  }

  @Async
  private void routeToPartner(TVAlertDto tvAlertDto) {
    try {
      if (tvAlertDto.getRouting() != null && tvAlertDto.getRouting().equals("true")) {
        routingClient.routeAlert(tvAlertDto);
      }
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }
}
