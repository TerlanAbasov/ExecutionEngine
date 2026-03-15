package com.quant.finance.execution.service;

import static com.ib.client.Util.DoubleMaxString;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ib.client.Contract;
import com.ib.client.Decimal;
import com.quant.finance.execution.client.IBClient;
import com.quant.finance.execution.config.ApplicationProperties;
import com.quant.finance.execution.model.ContractData;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PositionServiceNew {
  private final NotificationService notificationService;
  private final ObjectMapper objectMapper;
  private final ApplicationProperties properties;
  private final PnlService pnlService;

  @Lazy
  @Autowired
  private IBClient ibClient;

  private static final Map<String, ContractData> positionMap = new ConcurrentHashMap<>();
  private static final Map<String, CompletableFuture<ContractData>> positionFutureMap =
      new ConcurrentHashMap<>();

  //private volatile CompletableFuture<Map<String, ContractData>> positionsFuture;
  private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

  /**
   * called before trading to check if position of symbol existing or not.
   *
   * @param symbol
   * @return
   */
  public CompletableFuture<ContractData> requestPositions(String symbol) {
    synchronized (positionFutureMap) {
      positionFutureMap.putIfAbsent(symbol, new CompletableFuture<>());
    }
    ibClient.requestPositions();

    return positionFutureMap.get(symbol);
  }

  /**
   * called by api or webhook to get update and notification about positions
   */
  public void requestPositions() {
    synchronized (positionMap) {
      positionMap.clear();
    }
    ibClient.requestPositions();
  }

  public void onPosition(String account, Contract contract, Decimal quantity,
                         double avgCost) {

    try {
      log.info("POSITION. Account={}, symbol={}, conid={}, secType={}, currency={}," +
              " position={} , avgCost={}",
          account, contract.symbol(), contract.conid(), contract.secType().name(),
          contract.currency(), quantity.toString(), DoubleMaxString(avgCost));

      ContractData contractData = buildContractData(contract, quantity, avgCost);

      CompletableFuture<ContractData> future = positionFutureMap.remove(contract.symbol());
      if (future != null && !future.isDone()) {
        future.complete(contractData);
      }

      if (contractData.getQuantity() != 0) {
        positionMap.put(contractData.getSymbol(), contractData);
        pnlService.requestPnLForPositions(contractData);
        //todo find out when to remove contractData from the map
      }

    } catch (Exception e) {
      log.error(e.getMessage(), e);
      notificationService.notify(e.getMessage());
    }
  }

  private static ContractData buildContractData(Contract contract, Decimal quantity,
                                                double avgCost) {
    ContractData contractData = ContractData.builder()
        .symbol(contract.symbol())
        .securityType(contract.getSecType())
        .contractId(contract.conid())
        .currency(contract.currency())
        .averageCost(avgCost)
        .quantity(quantity.value().doubleValue())
        .build();
    return contractData;
  }

  public void onPositionEnd() {
    log.info("POSITION END");


  }

  public void onPositionEnd11() {
    try {
      positionMap.clear();
      scheduler.schedule(pnlService::checkSinglePnlCompletion, 5, TimeUnit.SECONDS);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      notificationService.notify(e.getMessage());
    }
  }


}
