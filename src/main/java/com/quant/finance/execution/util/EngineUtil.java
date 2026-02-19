package com.quant.finance.execution.util;

import java.util.concurrent.ThreadLocalRandom;

public class EngineUtil {
  public static synchronized int nextRequestId() {
    return ThreadLocalRandom.current().nextInt();
  }
}
