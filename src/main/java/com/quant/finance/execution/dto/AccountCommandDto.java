package com.quant.finance.execution.dto;

import lombok.Data;

@Data
public class AccountCommandDto {
  private Message message;

  @Data
  public static class Message {
    private Long message_id;
    private Chat chat;
    private String command;
  }

  @Data
  public static class Chat {
    private Long id;
    private String type;
  }
}
