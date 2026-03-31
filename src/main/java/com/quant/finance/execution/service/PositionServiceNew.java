package com.quant.finance.execution.service;

import static com.ib.client.Util.DoubleMaxString;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.ib.client.Contract;
import com.ib.client.Decimal;
import com.quant.finance.execution.client.IBClient;
import com.quant.finance.execution.config.ApplicationProperties;
import com.quant.finance.execution.model.Position;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
public class PositionServiceNew {
  private final NotificationService notificationService;
  private final ObjectMapper objectMapper;
  private final ApplicationProperties properties;
  private final PnlServiceNew pnlService;

  SimpleBeanPropertyFilter filter = SimpleBeanPropertyFilter
      .serializeAllExcept("contractId", "securityType", "currency");

  SimpleFilterProvider filters = new SimpleFilterProvider()
      .addFilter("positionFilter", filter);

  @Lazy
  @Autowired
  private IBClient ibClient;

  private final Map<String, Position> positionMap = new ConcurrentHashMap<>();

  /**
   * called before trading to check if position of symbol existing or not.
   *
   * @param symbol
   * @return
   */
  public Position getPositionBySymbol(String symbol) {
    return positionMap.get(properties.getAccount().getId() + ":" + symbol);
  }

  /**
   * called by api or webhook to syncronize positions
   */

  public void syncronizePositions() {
    ibClient.requestPositions();
  }

  public void onPosition(String account, Contract contract, Decimal quantity, double avgCost) {
    try {
      log.info("POSITION. Account={}, symbol={}, conid={}, secType={}, currency={}," +
              " position={} , avgCost={}",
          account, contract.symbol(), contract.conid(), contract.secType().name(),
          contract.currency(), quantity.toString(), DoubleMaxString(avgCost));

      Position position = Position.buildPosition(contract, quantity, avgCost);

      if (position.getQuantity() != 0) {
        positionMap.put(account + ":" + contract.symbol(), position);
        pnlService.requestPnLForPosition(position);
      }
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      notificationService.notify(e.getMessage());
    }
  }

  public void onPositionEnd() {
    log.info("POSITION END");
  }

  @Async
  public void sendPositions() {
    try {
      List<Position> allPositions = new ArrayList<>(positionMap.values());

      String header = String.format("Positions of %s\n", properties.getAccount().getId());

      for (int i = 0; i < allPositions.size(); i += 20) {
        List<Position> chunk = allPositions.subList(i, Math.min(i + 20, allPositions.size()));
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
