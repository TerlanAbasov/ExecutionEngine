package com.quant.finance.execution.enums;

public enum KillSwitchAction {
  BLOCK_NEW_ORDERS,       // stop accepting new alerts/orders
  CANCEL_OPEN_ORDERS,     // cancel GTC/pending orders via IB
  CLOSE_POSITIONS,        // market-sell all open positions
  ALL                     // cancel + close + block
}
