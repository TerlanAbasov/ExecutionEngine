package com.quant.finance.execution.util;

import com.ib.client.ContractDetails;
import com.ib.client.Util;
import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;

public class EngineUtil {
  public static synchronized int nextRequestId() {
    return ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE);
  }

  public static BigDecimal doubleToBigDecimal(double value) {
    return new BigDecimal(Util.DoubleMaxString(value, "0"));
  }

  public static BigDecimal doubleToBigDecimal(double value, String defaultValue) {
    return new BigDecimal(Util.DoubleMaxString(value, defaultValue));
  }

  public static boolean isDigit(String s) {
    return s != null && s.matches("\\d+");
  }

  public static double snapToTick(double price, ContractDetails contractDetails) {
    //todo Upgrade to MarketRule tick system
    //double tick = contractDetails.minTick();
    double tick = price >= 1 ? 0.01 : 0.0001;
    return Math.round(price / tick) * tick;
  }
}
