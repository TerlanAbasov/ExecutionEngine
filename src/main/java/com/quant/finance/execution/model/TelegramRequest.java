package com.quant.finance.execution.model;

import lombok.Data;

@Data
public class TelegramRequest {
  private String chat_id;
  private String text;
  private String parse_mode;
}
