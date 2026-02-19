package com.quant.finance.execution.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ib.client.Contract;
import com.ib.client.Decimal;
import com.ib.client.Util;
import com.quant.finance.execution.model.ContractData;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PositionService {
  private final EWrapperImpl eWrapper;
  private final NotificationService notificationService;
  private final ObjectMapper objectMapper;

  public PositionService(@Lazy EWrapperImpl eWrapper, NotificationService notificationService,
                         ObjectMapper objectMapper) {
    this.eWrapper = eWrapper;
    this.notificationService = notificationService;
    this.objectMapper = objectMapper;
  }

  private final Map<String, ContractData> positionMap = new ConcurrentHashMap<>();
  private CompletableFuture<Map<String, ContractData>> positionsFuture;

  public CompletableFuture<Map<String, ContractData>> requestPositions() {
    positionMap.clear();
    positionsFuture = new CompletableFuture<>();
    eWrapper.getEClientSocket().reqPositions();

    return positionsFuture;
  }

  public void onPosition(String account, Contract contract, Decimal quantity, double avgCost) {
    log.info(
        "POSITION. Account: {}, symbol: {}, conid: {}, secType: {}. currency: {}, position: {} , avgCost: {}",
        account, contract.symbol(), contract.conid(), contract.secType().name(),
        contract.currency(), quantity.toString(), Util.DoubleMaxString(avgCost));

    ContractData contractData =
        ContractData.builder().symbol(contract.symbol()).securityType(contract.getSecType())
            .contractId(contract.conid()).currency(contract.currency()).averageCost(avgCost)
            .quantity(quantity.value().doubleValue()).build();

    positionMap.put(contractData.getSymbol(), contractData);
  }

  public void onPositionEnd() {
    log.info("POSITION END");

    if (this.positionsFuture != null) {
      positionsFuture.complete(positionMap);
    }

    try {
      String message =
          objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(positionMap);
      notificationService.notify("Positions: " + message);
      positionMap.clear();
    } catch (JsonProcessingException e) {
      log.error(e.getMessage(), e);
    }
  }

}
