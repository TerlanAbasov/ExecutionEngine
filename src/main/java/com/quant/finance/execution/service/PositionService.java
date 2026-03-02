package com.quant.finance.execution.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ib.client.Contract;
import com.ib.client.Decimal;
import com.ib.client.Util;
import com.quant.finance.execution.client.IBClient;
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
  @Lazy
  @Autowired
  private IBClient ibClient;

  private final Map<String, ContractData> positionMap = new ConcurrentHashMap<>();
  private CompletableFuture<Map<String, ContractData>> positionsFuture;

  public CompletableFuture<Map<String, ContractData>> requestPositions() {
    positionMap.clear();
    positionsFuture = new CompletableFuture<>();
    ibClient.requestPositions();

    return positionsFuture;
  }

  public void onPosition(String account, Contract contract, Decimal quantity, double avgCost) {
    try {
      log.info(
          "POSITION. Account: {}, symbol: {}, conid: {}, secType: {}. currency: {}, position: {} , avgCost: {}",
          account, contract.symbol(), contract.conid(), contract.secType().name(),
          contract.currency(), quantity.toString(), Util.DoubleMaxString(avgCost));

      ContractData contractData =
          ContractData.builder().symbol(contract.symbol()).securityType(contract.getSecType())
              .contractId(contract.conid()).currency(contract.currency()).averageCost(avgCost)
              .quantity(quantity.value().doubleValue()).build();

      if (contractData.getQuantity() != 0) {
        positionMap.put(contractData.getSymbol(), contractData);
      }
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      notificationService.notify(e.getMessage());
    }

    //todo cancel open child orders when close parent order
  }

  public void onPositionEnd() {
    log.info("POSITION END");

    try {
      if (this.positionsFuture != null) {
        positionsFuture.complete(positionMap);
      }

      String message =
          objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(positionMap);
      notificationService.notify("Positions: " + message);
      positionMap.clear();
    } catch (JsonProcessingException e) {
      log.error(e.getMessage(), e);
      notificationService.notify(e.getMessage());
    }
  }

}
