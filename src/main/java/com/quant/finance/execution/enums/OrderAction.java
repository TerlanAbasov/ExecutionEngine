package com.quant.finance.execution.enums;

public enum OrderAction {
  BUY, SELL, UNKNOWN;

  private OrderAction() {
  }

  public static OrderAction get(String var0) {
    OrderAction[] var1 = values();
    int var2 = var1.length;

    for (int var3 = 0; var3 < var2; ++var3) {
      OrderAction var4 = var1[var3];
      if (var4.name().equalsIgnoreCase(var0)) {
        return var4;
      }
    }

    return UNKNOWN;
  }
}
