package com.quant.finance.execution.util;

import com.ib.client.Util;
import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;

public class EngineUtil {
  public static synchronized int nextRequestId() {
    return ThreadLocalRandom.current().nextInt();
  }

  public static BigDecimal doubleToBigDecimal(double value) {
    return new BigDecimal(Util.DoubleMaxString(value, "0"));
  }

  public static BigDecimal doubleToBigDecimal(double value, String defaultValue) {
    return new BigDecimal(Util.DoubleMaxString(value, defaultValue));
  }
}
