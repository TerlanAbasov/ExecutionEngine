package com.quant.finance.execution.service;

import com.quant.finance.execution.entity.StrategyEntity;
import com.quant.finance.execution.repository.StrategyRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StrategyService {
  private final StrategyRepository repository;
  private final NotificationService notificationService;

  @Transactional(readOnly = true)
  public Optional<StrategyEntity> findStrategyByName(String name) {
    return repository.findByName(name);
  }
}
