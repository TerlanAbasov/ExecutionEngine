package com.quant.finance.execution.model;

import com.ib.client.Types.Action;
import com.ib.client.Types.TimeInForce;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TradeCommand {
  private BotCommand command;
  private String symbol;
  private String strategy;
  private Action action;
  private Double quantity;
  private String orderType;
  private Double limitPrice;
  private TimeInForce tif;
  private String rawText;
  private Long chatId;
}
