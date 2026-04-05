package com.quant.finance.execution.service;

import com.ib.client.Types;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TradeExecutorFactory {

  private final List<TradeExecutor> executors;

  public TradeExecutor resolve(Types.SecType secType) {
    return executors.stream()
        .filter(e -> e.supports(secType))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("No executor found"));
  }
}