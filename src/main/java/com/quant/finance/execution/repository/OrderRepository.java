package com.quant.finance.execution.repository;

import com.ib.client.OrderStatus;
import com.quant.finance.execution.entity.OrderEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
  Optional<OrderEntity> findByBrokerOrderId(int brokerOrderId);

  List<OrderEntity> findBySymbol(String symbol);

  List<OrderEntity> findBySymbolAndStatusIn(String symbol, List<OrderStatus> statuses);

  Optional<OrderEntity> findByExecutions_ExecId(String executionId);

  @Query("SELECT MAX(o.brokerOrderId) FROM OrderEntity o")
  Integer findMaxBrokerOrderId();

}
