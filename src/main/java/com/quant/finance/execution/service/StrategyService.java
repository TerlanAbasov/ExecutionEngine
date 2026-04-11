package com.quant.finance.execution.service;

import com.ib.client.Types.Action;
import com.quant.finance.execution.entity.AlertEntity;
import com.quant.finance.execution.entity.StrategyEntity;
import com.quant.finance.execution.enums.AlertState;
import com.quant.finance.execution.model.Position;
import com.quant.finance.execution.repository.StrategyRepository;
import com.quant.finance.execution.util.EngineUtil;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
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
    try {
      Optional<StrategyEntity> optionalStrategy = checkStrategy(alert);
      if (optionalStrategy.isEmpty()) {
        alertService.updateStateAndDescription(
            alert, AlertState.FAILED, "Strategy not found.");
        return;
      }

      String symbol = alert.getSymbol();
      if (alert.getAssetClass().equals("CRYPTO")) {
        symbol = EngineUtil.extractCryptoBaseSymbol(alert.getSymbol());
      }

      Position position = positionService.getPositionBySymbol(symbol);

      log.info("Position validation. alertSymbol={}, existingPosition={}", symbol, position);

      if (!checkIfQuantityExecutable(alert, position)) {
        alertService.updateStateAndDescription(alert, AlertState.PROCESSED,
            "Quantity check is false.");
        return;
      }

      contractService.requestContract(alert)
          .orTimeout(30, TimeUnit.SECONDS)
          .thenAccept(contractDetails -> {

            tradeService.trade(alert, optionalStrategy.get(), contractDetails,
                position);

          })
          .exceptionally(ex -> {
            log.error("Contract request failed for {}", alert.getSymbol(), ex);
            notificationService.notify("Contract request failed: " + ex.getMessage());
            return null;
          });
    } catch (Exception e) {
      log.error("Failed to request positions", e);
      notificationService.notify("Failed to request positions: " + e.getMessage());
      return;
    }
  }

  private boolean checkIfQuantityExecutable(AlertEntity alert, Position position) {
    Action action = alert.getAction();
    double existingQuantity = position != null ? position.getQuantity() : 0d;

    boolean executable = (Action.BUY.equals(action) && existingQuantity <= 0)
        || (Action.SELL.equals(action) && existingQuantity > 0);

    if (!executable) {
      String message = String.format(
          "Quantity check is false. symbol=%s, peerSymbol=%s, action=%s, existingQuantity=%.2f",
          alert.getSymbol(), alert.getPeerSymbol(), action, existingQuantity);
      log.info(message);
      notificationService.notify(message);
    }

    return executable;
  }

  public Optional<StrategyEntity> checkStrategy(AlertEntity alert) {
    Optional<StrategyEntity> optionalStrategy = repository.findByName(alert.getStrategy());

    if (optionalStrategy.isEmpty()) {
      String mesage = String.format("Strategy: '%s' not found", alert.getStrategy());
      log.error(mesage);
      notificationService.notify(mesage);
      return Optional.empty();
    }

    StrategyEntity strategy = optionalStrategy.get();
    if (strategy.getMaxPositionAmount().compareTo(alert.getHigh()) < 0) {
      log.error("Strategy: '{}'. MaxPositionAmount'{}' is less than price .", alert.getStrategy(),
          strategy.getMaxPositionAmount());

      return Optional.empty();
    }

    return optionalStrategy;
  }
}
