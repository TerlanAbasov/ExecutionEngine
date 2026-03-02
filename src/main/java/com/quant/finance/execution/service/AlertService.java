package com.quant.finance.execution.service;

import com.quant.finance.execution.dto.TVAlertDto;
import com.quant.finance.execution.entity.AlertEntity;
import com.quant.finance.execution.mapper.AlertMapper;
import com.quant.finance.execution.repository.AlertRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class AlertService {
  private final AlertRepository repository;
  private final AlertMapper alertMapper;
  private final StrategyService strategyService;
  private final NotificationService notificationService;

  public AlertService(AlertRepository repository, AlertMapper alertMapper,
                      @Lazy StrategyService strategyService,
                      NotificationService notificationService) {
    this.repository = repository;
    this.alertMapper = alertMapper;
    this.strategyService = strategyService;
    this.notificationService = notificationService;
  }

  public void processAlert(TVAlertDto tvAlertDto) {
    log.info("Processing alert {}", tvAlertDto);
    notificationService.notify(tvAlertDto);
    AlertEntity alert = repository.save(alertMapper.toEntity(tvAlertDto));

    strategyService.executeStrategy(alert);
  }

  @Transactional
  public AlertEntity save(TVAlertDto tvAlertDto) {
    return repository.save(alertMapper.toEntity(tvAlertDto));
  }
}
