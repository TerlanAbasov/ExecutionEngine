package com.quant.finance.execution.repository;

import com.quant.finance.execution.entity.KillSwitchEntity;
import com.quant.finance.execution.enums.KillSwitchScope;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KillSwitchRepository {//extends JpaRepository<KillSwitchEntity, Long> {

  // Check for active global kill
  //Optional<KillSwitchEntity> findByScopeAndActiveTrue(KillSwitchScope scope);

  // Check strategy-level or symbol-level kill
  //Optional<KillSwitchEntity> findByScopeAndScopeValueAndActiveTrue(
  //    KillSwitchScope scope, String scopeValue);

  // Load all active kills (used at startup for recovery)
  //List<KillSwitchEntity> findAllByActiveTrue();
}
