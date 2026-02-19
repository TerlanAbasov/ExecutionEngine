package com.quant.finance.execution.repository;

import com.quant.finance.execution.entity.StrategyEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StrategyRepository extends JpaRepository<StrategyEntity, Integer> {
  public Optional<StrategyEntity> findByName(String name);
}
