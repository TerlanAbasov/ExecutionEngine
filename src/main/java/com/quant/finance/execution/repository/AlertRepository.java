package com.quant.finance.execution.repository;

import com.quant.finance.execution.entity.AlertEntity;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AlertRepository extends JpaRepository<AlertEntity, Long> {

  @Query("select a from AlertEntity a where a.state='RECEIVED' ORDER BY a.generatedTime")
  List<AlertEntity> findNextAlerts(Pageable pageable);
}
