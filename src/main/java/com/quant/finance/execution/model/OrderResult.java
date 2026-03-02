package com.quant.finance.execution.model;

import com.ib.client.Types;
import com.ib.client.Types.Action;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderResult {
  private boolean success;
  private String message;
  private String symbol;
  private Action side;
  private Double quantity;
  private Double price;
  private String orderId;
}
