package com.quant.finance.execution.error;

public class ExecutionNotFoundException extends RuntimeException {

  public ExecutionNotFoundException(String message) {
    super(message);
  }
}
