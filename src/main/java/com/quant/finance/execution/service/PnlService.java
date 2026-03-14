package com.quant.finance.execution.service;

import static com.ib.client.Util.DoubleMaxString;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ib.client.Decimal;
import com.quant.finance.execution.client.IBClient;
import com.quant.finance.execution.config.ApplicationProperties;
import com.quant.finance.execution.model.ContractData;
import com.quant.finance.execution.util.EngineUtil;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PnlService {
  private final IBClient ibClient;
  private final NotificationService notificationService;
  private final ObjectMapper objectMapper;
  private final ApplicationProperties properties;

  private final Map<Integer, ContractData> pnlMap = new ConcurrentHashMap<>();
  private final Set<Integer> pendingPnl = ConcurrentHashMap.newKeySet();

  public void pnlSingle(int requestId, Decimal positions, double dailyPnL,
                        double unrealizedPnl,
                        double realizedPnl, double value) {
    ContractData contractData = pnlMap.get(requestId);

    if (contractData == null) {
      log.warn("Contract not found for requestId={}", requestId);
      log.info("pnlSingle. requestId={}, positions={}, dailyPnL={}," +
              " unrealizedPnl={}, realizedPnl={}, value={}",
          requestId, positions, DoubleMaxString(dailyPnL), DoubleMaxString(unrealizedPnl),
          DoubleMaxString(realizedPnl), value);
    } else {

      log.info("pnlSingle. symbol={}, conId={}. requestId={}, positions={}, dailyPnL={}," +
              " unrealizedPnl={}, realizedPnl={}, value={}",
          contractData.getSymbol(), contractData.getContractId(), requestId, positions,
          DoubleMaxString(dailyPnL), DoubleMaxString(unrealizedPnl),
          DoubleMaxString(realizedPnl), value);

      contractData.setQuantity(positions.value().doubleValue());
      contractData.setDailyPnL(DoubleMaxString(dailyPnL));
      contractData.setUnrealizedPnl(DoubleMaxString(unrealizedPnl));
      contractData.setRealizedPnl(DoubleMaxString(realizedPnl));
      contractData.setValue(value);
    }


    pendingPnl.remove(requestId);
    ibClient.getEClientSocket().cancelPnLSingle(requestId);
  }

  public void pnl(int requestId, double dailyPnL, double unrealizedPnl, double realizedPnl) {
    String message = String.format("PnL. dailyPnL=%f, unrealizedPnl=%f, realizedPnl=%f",
        dailyPnL, unrealizedPnl, realizedPnl);
    log.info(message);
    notificationService.notify(message);
  }

  public void requestPnLForPositions(ContractData contractData) {
    int requestId = EngineUtil.nextRequestId();
    pnlMap.put(requestId, contractData);
    pendingPnl.add(requestId);

    ibClient.requestSinglePnl(
        requestId,
        properties.getAccount().getId(),
        "",
        contractData.getContractId());
  }

  public void checkSinglePnlCompletion() {
    try {
      String message =
          objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(pnlMap.values());

      if (pendingPnl.isEmpty()) {
        notificationService.notify("Positions: " + message);
      } else {
        notificationService.notify("Partial snapshot received. Positions: " + message);
      }
    } catch (JsonProcessingException e) {
      log.error("Failed to serialize positions", e);
      notificationService.notify(e.getMessage());
    } finally {
      pnlMap.clear();
    }
  }
}
