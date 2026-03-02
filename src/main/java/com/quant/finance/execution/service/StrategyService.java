package com.quant.finance.execution.service;

import com.ib.client.Types.Action;
import com.quant.finance.execution.entity.AlertEntity;
import com.quant.finance.execution.entity.StrategyEntity;
import com.quant.finance.execution.model.ContractData;
import com.quant.finance.execution.repository.StrategyRepository;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StrategyService {
  private final StrategyRepository repository;
  private final NotificationService notificationService;
  private final PositionService positionService;
  private final AlertService alertService;
  private final ContractService contractService;
  private final TradeService tradeService;

  public void executeStrategy(AlertEntity alert) {

    Optional<StrategyEntity> strategy = checkStrategy(alert);
    if (strategy.isEmpty()) {
      return;
    }

    Map<String, ContractData> positions = null;
    try {
      positions = positionService.requestPositions().get();
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }

    Double quantity =
        positions.getOrDefault(alert.getSymbol(), ContractData.builder().quantity(0d).build())
            .getQuantity();

    if (quantity > 0 && Action.BUY.equals(alert.getAction())) {
      String message = String.format("Can't buy existing symbol: %s, action: %s", alert.getSymbol(),
          alert.getAction());
      log.error(message);
      notificationService.notify(message);
      return;
    } else if (quantity <= 0 && Action.SELL.equals(alert.getAction())) {
      String message =
          String.format("Can't sell non existing symbol: %s, action: %s", alert.getSymbol(),
              alert.getAction());
      log.error(message);
      notificationService.notify(message);
      return;
    }

    contractService.requestContract(alert.getSymbol())
        .thenAccept(contract -> {
          tradeService.trade(alert, strategy.get(), contract, quantity);
        });
  }

  public Optional<StrategyEntity> checkStrategy(AlertEntity alert) {
    Optional<StrategyEntity> optionalStrategy = repository.findByName(alert.getStrategy());

    if (optionalStrategy.isEmpty()) {
      log.error("Strategy: '{}' does not exits.", alert.getStrategy());
      notificationService.notify(
          String.format("Strategy: '%s' does not exits.", alert.getStrategy()));
    } else if (optionalStrategy.get().getMaxPositionAmount().compareTo(alert.getHigh()) == -1) {
      log.error("Strategy: '{}'. MaxPositionAmount is less than price .", alert.getStrategy());
    }
    return optionalStrategy;
  }

}
