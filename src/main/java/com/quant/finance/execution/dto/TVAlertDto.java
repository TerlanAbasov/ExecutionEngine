package com.quant.finance.execution.dto;

import lombok.Data;

@Data
public class TVAlertDto {
  private String ticker;
  private String buy;
  private String sell;
  private String peerTicker;
  private String strategy;
  private String routing;
  private String exchange;
  private String interval;
  private String time;
  private String timenow;
  private String volume;
  private String close;
  private String high;
  private String low;
  private String open;
  private String quote;
  private String base;
  private String plot0;
  private String plot1;
}
