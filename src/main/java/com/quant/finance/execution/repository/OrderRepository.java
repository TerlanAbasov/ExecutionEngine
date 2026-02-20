package com.quant.finance.execution.repository;

import com.quant.finance.execution.entity.OrderEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
  Optional<OrderEntity> findByBrokerOrderId(String brokerOrderId);

  Optional<OrderEntity> findByExecutions_ExecId(String executionId);
}
