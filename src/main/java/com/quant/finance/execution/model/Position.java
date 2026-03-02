package com.quant.finance.execution.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Position {
  private String symbol;
  private String side;
  private Double quantity;
  private Double entryPrice;
  private Double currentPrice;
  private Double pnl;
}