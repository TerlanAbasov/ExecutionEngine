package com.quant.finance.execution.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DiscordMessage {
  private String content;
  private String username;
}
