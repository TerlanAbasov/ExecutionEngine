package com.quant.finance.execution.dto;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderCancelDto {
  @NotNull
  private String symbol;
  private Integer orderId;
}
