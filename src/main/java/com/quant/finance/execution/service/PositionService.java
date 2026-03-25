package com.quant.finance.execution.service;

import static com.ib.client.Util.DoubleMaxString;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ib.client.Contract;
import com.ib.client.Decimal;
import com.quant.finance.execution.client.IBClient;
import com.quant.finance.execution.config.ApplicationProperties;
import com.quant.finance.execution.model.ContractData;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PositionService {
  private final NotificationService notificationService;
  private final ObjectMapper objectMapper;
  private final ApplicationProperties properties;
  private final PnlService pnlService;

  @Lazy
  @Autowired
  private IBClient ibClient;

  private final Map<String, ContractData> positionMap = new ConcurrentHashMap<>();
  private final Map<String, CompletableFuture<ContractData>> positionFutureMap =
      new ConcurrentHashMap<>();

  /**
   * called before trading to check if position of symbol existing or not.
   *
   * @param symbol
   * @return
   */
  public CompletableFuture<ContractData> getSymbolPosition(String symbol) {
    CompletableFuture<ContractData> future = new CompletableFuture<>();
    synchronized (positionFutureMap) {
      positionFutureMap.putIfAbsent(symbol, future);
    }
    pnlService.clearPnlCollections();
    ibClient.requestPositions();

    return future;
  }

  /**
   * called by api or webhook to get update and notification about positions
   */
  public void requestPositions() {
    synchronized (this) {
      //positionMap.clear();
      pnlService.clearPnlCollections();
    }
    ibClient.requestPositions();
  }

  public void onPosition(String account, Contract contract, Decimal quantity, double avgCost) {
    try {
      log.info("POSITION. Account={}, symbol={}, conid={}, secType={}, currency={}," +
              " position={} , avgCost={}",
          account, contract.symbol(), contract.conid(), contract.secType().name(),
          contract.currency(), quantity.toString(), DoubleMaxString(avgCost));

      ContractData contractData = ContractData.buildContractData(contract, quantity, avgCost);

      synchronized (positionFutureMap) {
        CompletableFuture<ContractData> future = positionFutureMap.get(contract.symbol());
        if (future != null && !future.isDone()) {
          log.warn("Completing future");
          future.complete(contractData);
          positionFutureMap.remove(contract.symbol());
        }

        if (contractData.getQuantity() != 0) {
          pnlService.requestPnLForPosition(contractData);
        }
      }

    } catch (Exception e) {
      log.error(e.getMessage(), e);
      notificationService.notify(e.getMessage());
    }
  }

  public void onPositionEnd() {
    log.info("POSITION END");
    pnlService.notifyAboutPositionsAndPnL();

    synchronized (positionFutureMap) {
      positionFutureMap.forEach((symbol, future) -> {
        if (!future.isDone()) {
          log.warn("No position found for {}, completing with null", symbol);

          future.complete(null);
        }
      });
      positionFutureMap.clear();
    }
  }
}
