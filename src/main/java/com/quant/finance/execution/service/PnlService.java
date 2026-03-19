package com.quant.finance.execution.service;

import static com.ib.client.Util.DoubleMaxString;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.ib.client.Decimal;
import com.quant.finance.execution.client.IBClient;
import com.quant.finance.execution.config.ApplicationProperties;
import com.quant.finance.execution.model.ContractData;
import com.quant.finance.execution.util.EngineUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PnlService {
  private final NotificationService notificationService;
  private final ObjectMapper objectMapper;
  private final ApplicationProperties properties;
  @Lazy
  @Autowired
  private IBClient ibClient;

  SimpleBeanPropertyFilter filter = SimpleBeanPropertyFilter
      .serializeAllExcept("contractId", "securityType", "currency");

  SimpleFilterProvider filters = new SimpleFilterProvider()
      .addFilter("contractFilter", filter);

  private final Map<Integer, ContractData> pnlMap = new ConcurrentHashMap<>();
  private final Set<Integer> pendingPnl = ConcurrentHashMap.newKeySet();

  public void clearPnlCollections() {
    pnlMap.clear();
    pendingPnl.clear();
  }

  public void requestPnLForPosition(ContractData contractData) {
    int requestId = EngineUtil.nextRequestId();

    pendingPnl.add(requestId);
    pnlMap.put(requestId, contractData);

    ibClient.requestSinglePnl(
        requestId, properties.getAccount().getId(), "", contractData.getContractId());
  }

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
      contractData.setDailyPnL(ContractData.scaleDoubleValue(DoubleMaxString(dailyPnL)));
      contractData.setUnrealizedPnl(ContractData.scaleDoubleValue(DoubleMaxString(unrealizedPnl)));
      contractData.setRealizedPnl(ContractData.scaleDoubleValue(DoubleMaxString(realizedPnl)));
      contractData.setValue(ContractData.scaleDoubleValue(value));
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

  @Async
  public void notifyAboutPositionsAndPnL() {
    try {
      boolean isCompleted = false;

      for (int i = 0; i < 10; i++) {
        isCompleted = pendingPnl.isEmpty();
        if (isCompleted) {
          break;
        } else {
          Thread.sleep(1000);
        }
      }

      List<ContractData> allPositions = new ArrayList<>(pnlMap.values());
      pnlMap.clear();

      String header = isCompleted
          ? "Positions:\n"
          : "Partial snapshot received. Positions:\n";

      for (int i = 0; i < allPositions.size(); i += 20) {
        List<ContractData> chunk = allPositions.subList(i, Math.min(i + 20, allPositions.size()));
        String positionsChunk =
            objectMapper.writer(filters).withDefaultPrettyPrinter().writeValueAsString(chunk);

        String message = (i == 0 ? header : "") + positionsChunk;
        log.info("{}", message);
        notificationService.notify(message);
      }
    } catch (Exception e) {
      log.error("Failed to serialize positions", e);
      notificationService.notify(e.getMessage());
    }
  }

}
