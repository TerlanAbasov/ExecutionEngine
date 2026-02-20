package com.quant.finance.execution.service;

import com.quant.finance.execution.entity.AlertEntity;
import com.quant.finance.execution.entity.StrategyEntity;
import com.quant.finance.execution.enums.OrderAction;
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

    Optional<StrategyEntity> strategy = checkStrategy(alert.getStrategy());
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

    if (quantity > 0 && OrderAction.BUY.equals(alert.getAction())) {
      log.error("Can't buy existing symbol: {}, action: {}, quantity: {}", alert.getSymbol(),
          alert.getAction(), quantity);
      return;
    } else if (quantity <= 0 && OrderAction.SELL.equals(alert.getAction())) {
      log.error("Can't sell non existing symbol: {}, action: {}, quantity: {}", alert.getSymbol(),
          alert.getAction(), quantity);
      return;
    }

    contractService.requestContract(alert.getSymbol())
        .thenAccept(contract -> {
          tradeService.placeOrder(alert, strategy.get(), contract);
        });
  }

  public Optional<StrategyEntity> checkStrategy(String strategyName) {
    Optional<StrategyEntity> optionalStrategy = repository.findByName(strategyName);

    if (optionalStrategy.isEmpty()) {
      log.error("Strategy: '{}' does not exits.", strategyName);
      notificationService.notify(String.format("Strategy: '%s' does not exits.", strategyName));
    }

    return optionalStrategy;
  }

}
