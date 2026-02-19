package com.quant.finance.execution.repository;

import com.quant.finance.execution.entity.AlertEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertRepository extends JpaRepository<AlertEntity, Long> {
}
