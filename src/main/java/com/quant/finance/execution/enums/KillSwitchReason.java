package com.quant.finance.execution.enums;

public enum KillSwitchReason {
  MANUAL,             // operator pressed the button
  MAX_LOSS,           // PnL threshold breached
  MAX_ORDERS,         // order rate exceeded
  ERROR_THRESHOLD,    // too many consecutive errors
  SCHEDULED           // time-based window
}
