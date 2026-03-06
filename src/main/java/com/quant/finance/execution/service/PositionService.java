package com.quant.finance.execution.service;

import static com.ib.client.Util.DoubleMaxString;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ib.client.Contract;
import com.ib.client.Decimal;
import com.quant.finance.execution.client.IBClient;
import com.quant.finance.execution.config.ApplicationProperties;
import com.quant.finance.execution.model.ContractData;
import com.quant.finance.execution.util.EngineUtil;
import java.util.Map;
import java.util.Set;
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
public class PositionService {
  private final NotificationService notificationService;
  private final ObjectMapper objectMapper;
  private final ApplicationProperties properties;
  @Lazy
  @Autowired
  private IBClient ibClient;

  private final Map<String, ContractData> positionMap = new ConcurrentHashMap<>();
  private CompletableFuture<Map<String, ContractData>> positionsFuture;
  private final Map<Integer, ContractData> pnlMap = new ConcurrentHashMap<>();
  private final Set<Integer> pendingPnl = ConcurrentHashMap.newKeySet();
  private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

  public CompletableFuture<Map<String, ContractData>> requestPositions() {
    positionMap.clear();
    positionsFuture = new CompletableFuture<>();
    ibClient.requestPositions();

    return positionsFuture;
  }

  public void onPosition(String account, Contract contract, Decimal quantity, double avgCost) {
    try {
      log.info(
          "POSITION. Account: {}, symbol: {}, conid: {}, secType: {}," +
              " currency: {}, position: {} , avgCost: {}",
          account, contract.symbol(), contract.conid(), contract.secType().name(),
          contract.currency(), quantity.toString(), DoubleMaxString(avgCost));

      ContractData contractData = ContractData.builder()
          .symbol(contract.symbol())
          .securityType(contract.getSecType())
          .contractId(contract.conid())
          .currency(contract.currency())
          .averageCost(avgCost)
          .quantity(quantity.value().doubleValue())
          .build();

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

      positionMap.forEach((symbol, contractData) -> {
        int requestId = EngineUtil.nextRequestId();
        pnlMap.put(requestId, contractData);
        pendingPnl.add(requestId);

        ibClient.requestSinglePnl(requestId, properties.getAccount().getId(), "",
            contractData.getContractId());
      });

      //String message =
      //    objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(pnlMap.values());
      //notificationService.notify("Positions: " + message);
      positionMap.clear();
      scheduler.schedule(this::checkSinglePnlCompletion, 5, TimeUnit.SECONDS);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      notificationService.notify(e.getMessage());
    }
  }

  public void pnlSingle(int requestId, Decimal positions, double dailyPnL, double unrealizedPnl,
                        double realizedPnl, double value) {
    //String message = String.format(
    //    "PNL SINGLE. RequestId: %d, pos: %s, dailyPnl: %.2f, unrealizedPnl: %.2f, realizedPnL: %s, value: %.2f",
    //    requestId, pos.toString(), dailyPnL, unrealizedPnl,
    //    Util.DoubleMaxString(realizedPnl, "0.00"), value);
    //
    //log.info(message);
    //notificationService.notify(message);
    ContractData contractData = pnlMap.get(requestId);

    log.info(
        "pnlSingle. symbol: {}, conId: {}. requestId: {}, positions: {}, dailyPnL: {}," +
            " unrealizedPnl: {}, realizedPnl: {}, value: {}",
        contractData.getSymbol(), contractData.getContractId(), requestId, positions,
        DoubleMaxString(dailyPnL), DoubleMaxString(unrealizedPnl),
        DoubleMaxString(realizedPnl), value);

    contractData.setQuantity(positions.value().doubleValue());
    contractData.setDailyPnL(DoubleMaxString(dailyPnL));
    contractData.setUnrealizedPnl(DoubleMaxString(unrealizedPnl));
    contractData.setRealizedPnl(DoubleMaxString(realizedPnl));
    contractData.setValue(value);

    pendingPnl.remove(requestId);
    ibClient.getEClientSocket().cancelPnLSingle(requestId);
  }

  private void checkSinglePnlCompletion() {
    String message = null;
    try {
      message = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(pnlMap.values());

      if (pendingPnl.isEmpty()) {
        notificationService.notify("Positions: " + message);
      } else {
        notificationService.notify("Partial snapshot received. Positions: " + message);
      }
    } catch (JsonProcessingException e) {
      log.error(e.getMessage(), e);
      notificationService.notify(e.getMessage());
    } finally {
      pnlMap.clear();
    }
  }

  public void pnl(int requestId, double dailyPnL, double unrealizedPnl, double realizedPnl) {
    //String message =
    //    String.format("PNL. RequestId: %d, dailyPnl: %.2f, unrealizedPnl: %.2f, realizedPnl: %.2f",
    //        requestId, dailyPnL, unrealizedPnl, realizedPnl);
    //
    //log.info(message);
    //notificationService.notify(message);
  }

}
