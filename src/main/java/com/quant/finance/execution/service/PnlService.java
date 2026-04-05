package com.quant.finance.execution.service;

import static com.ib.client.Util.DoubleMaxString;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.ib.client.Decimal;
import com.quant.finance.execution.client.IBClient;
import com.quant.finance.execution.config.ApplicationProperties;
import com.quant.finance.execution.model.Position;
import com.quant.finance.execution.util.EngineUtil;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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
      .addFilter("positionFilter", filter);

  private final Map<Integer, Position> pnlMap = new ConcurrentHashMap<>();

  public void clearPnlCollections() {
    pnlMap.clear();
  }

  public void requestPnLForPosition(Position position) {
    int requestId = EngineUtil.nextRequestId();
    pnlMap.put(requestId, position);

    ibClient.requestSinglePnl(
        requestId, properties.getAccount().getId(), "", position.getContractId());
  }

  public void pnlSingle(int requestId, Decimal positions, double dailyPnL,
                        double unrealizedPnl,
                        double realizedPnl, double value) {
    Position position = pnlMap.get(requestId);

    if (position == null) {
      log.warn("Contract not found for requestId={}", requestId);
      log.info("pnlSingle. requestId={}, positions={}, dailyPnL={}," +
              " unrealizedPnl={}, realizedPnl={}, value={}",
          requestId, positions, DoubleMaxString(dailyPnL), DoubleMaxString(unrealizedPnl),
          DoubleMaxString(realizedPnl), value);
    } else {

      log.info("pnlSingle. symbol={}, conId={}. requestId={}, positions={}, dailyPnL={}," +
              " unrealizedPnl={}, realizedPnl={}, value={}",
          position.getSymbol(), position.getContractId(), requestId, positions,
          DoubleMaxString(dailyPnL), DoubleMaxString(unrealizedPnl),
          DoubleMaxString(realizedPnl), value);

      position.setQuantity(positions.value().doubleValue());
      position.setDailyPnL(Position.scaleDoubleValue(DoubleMaxString(dailyPnL)));
      position.setUnrealizedPnl(Position.scaleDoubleValue(DoubleMaxString(unrealizedPnl)));
      position.setRealizedPnl(Position.scaleDoubleValue(DoubleMaxString(realizedPnl)));
      position.setValue(Position.scaleDoubleValue(value));
    }

    ibClient.getEClientSocket().cancelPnLSingle(requestId);
  }

  public void pnl(int requestId, double dailyPnL, double unrealizedPnl, double realizedPnl) {
    String message = String.format("PnL. dailyPnL=%f, unrealizedPnl=%f, realizedPnl=%f",
        dailyPnL, unrealizedPnl, realizedPnl);
    log.info(message);
    notificationService.notify(message);
  }
}
