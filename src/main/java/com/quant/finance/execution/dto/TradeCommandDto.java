package com.quant.finance.execution.dto;

import com.ib.client.Types.Action;
import com.ib.client.Types.TimeInForce;
import com.quant.finance.execution.enums.BotCommand;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeCommandDto {
  private BotCommand command;
  private String identifier;
  private String strategy;
  private Action action;
  private Double quantity;
  private String orderType;
  private Double limitPrice;
  private TimeInForce tif;
  private Long chatId;
  private String rawText;

  public TradeCommandDto(BotCommand command, Long chatId, String rawText) {
    this.command = command;
    this.chatId = chatId;
    this.rawText = rawText;
  }
}
