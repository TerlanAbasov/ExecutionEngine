package com.quant.finance.execution.service;

import com.quant.finance.execution.dto.TVAlertDto;
import com.quant.finance.execution.entity.AlertEntity;
import com.quant.finance.execution.mapper.AlertMapper;
import com.quant.finance.execution.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AlertService {
  private final AlertRepository repository;
  private final AlertMapper alertMapper;

  @Transactional
  public AlertEntity save(TVAlertDto tvAlertDto) {
    return repository.save(alertMapper.toEntity(tvAlertDto));
  }
}
