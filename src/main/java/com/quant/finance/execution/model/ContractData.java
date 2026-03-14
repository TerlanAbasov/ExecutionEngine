package com.quant.finance.execution.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ContractData {
  private String symbol;
  private Integer contractId;
  private String securityType;
  private String currency;
  @Builder.Default
  private Double quantity = 0d;
  private Double averageCost;
  private String dailyPnL;
  private String unrealizedPnl;
  private String realizedPnl;
  private Double value;

  //todo consider should equals and hashcode be overriten.
}
