package com.quant.finance.execution.repository;

import com.quant.finance.execution.entity.AlertEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertRepository extends JpaRepository<AlertEntity, Long> {

  List<AlertEntity> findNext(int size);
}
