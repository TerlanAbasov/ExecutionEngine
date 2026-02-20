package com.quant.finance.execution.repository;

import com.quant.finance.execution.entity.ExecutionEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExecutionRepository extends JpaRepository<ExecutionEntity, Long> {

  Optional<ExecutionEntity> findByOrderId(int orderId);
  Optional<ExecutionEntity> findByExecId(String execId);
}
