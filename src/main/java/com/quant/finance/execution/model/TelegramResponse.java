package com.quant.finance.execution.model;

import lombok.Data;

@Data
public class TelegramResponse {
  private boolean ok;
  private Result result;

  @Data
  public static class Result {
    private Long message_id;
    private Long chat_id;
    private String text;
  }
}
