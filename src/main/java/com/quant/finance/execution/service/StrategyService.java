package com.quant.finance.execution.service;

import com.ib.client.Types.Action;
import com.quant.finance.execution.entity.AlertEntity;
import com.quant.finance.execution.entity.StrategyEntity;
import com.quant.finance.execution.enums.AlertState;
import com.quant.finance.execution.model.ContractData;
import com.quant.finance.execution.repository.StrategyRepository;
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

      positionService.getSymbolPosition(alert.getSymbol())
          .orTimeout(30, TimeUnit.SECONDS)
          .thenAccept(position -> {

            log.info("Position validation. alertSymbol={}, existingPosition={}",
                alert.getSymbol(), position);

            if (!checkIfQuantityExecutable(alert, position)) {
              alertService.updateStateAndDescription(alert, AlertState.PROCESSED,
                  "Quantity check is false.");
              return;
            }

            contractService.requestContract(alert.getSymbol())
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
          })
          .exceptionally(ex -> {
            log.error("Failed to request positions", ex);
            notificationService.notify("Failed to request positions: " + ex.getMessage());
            return null;
          });

    } catch (Exception e) {
      log.error("Failed to request positions", e);
      notificationService.notify("Failed to request positions: " + e.getMessage());
      return;
    }
  }

  private boolean checkIfQuantityExecutable(AlertEntity alert, ContractData position) {
    if (Action.BUY.equals(alert.getAction()) && (position == null || position.getQuantity() <= 0)) {
      return true;
    } else if (Action.SELL.equals(alert.getAction()) && position != null &&
        position.getQuantity() > 0) {
      return true;
    }

    double existingQuantity = position != null ? position.getQuantity() : 0d;

    String message =
        String.format(
            "Quantity check is false. symbol=%s, peerSymbol=%s, action=%s, existingQuantity=%.2f",
            alert.getSymbol(), alert.getPeerSymbol(), alert.getAction(), existingQuantity);
    log.info(message);
    notificationService.notify(message);

    return false;
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
      log.error("Strategy: '{}'. MaxPositionAmount is less than price .", alert.getStrategy());

      return Optional.empty();
    }

    return optionalStrategy;
  }

}
