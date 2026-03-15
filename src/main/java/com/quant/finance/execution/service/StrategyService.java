package com.quant.finance.execution.service;

import com.ib.client.Types.Action;
import com.quant.finance.execution.entity.AlertEntity;
import com.quant.finance.execution.entity.StrategyEntity;
import com.quant.finance.execution.model.ContractData;
import com.quant.finance.execution.repository.StrategyRepository;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StrategyService {
  private final StrategyRepository repository;
  private final NotificationService notificationService;
  private final PositionServiceNew positionService;
  private final AlertService alertService;
  private final ContractService contractService;
  private final TradeService tradeService;

  public void executeStrategy(AlertEntity alert) {
    Optional<StrategyEntity> optionalStrategy = checkStrategy(alert);
    if (optionalStrategy.isEmpty()) {
      return;
    }

    StrategyEntity strategy = optionalStrategy.get();

    double existingQuantity;

    try {
      CompletableFuture<ContractData> future = positionService.getSymbolPosition(alert.getSymbol());
      if (future == null || !future.isDone()) {
        String message =
            String.format("CompletableFuture is null or future is not completed. symbol=%s",
                alert.getSymbol());

        log.error(message);
        notificationService.notify(message);
        return;
      }

      ContractData existingPosition = future.get();

      existingQuantity = existingPosition != null ? existingPosition.getQuantity() : 0d;

      log.info("Position validation. alertSymbol={}, existingPosition={}",
          alert.getSymbol(), existingPosition);
    } catch (Exception e) {
      log.error("Failed to request positions", e);
      notificationService.notify("Failed to request positions: " + e.getMessage());
      return;
    }

    if (!checkIfQuantityExecutable(alert, existingQuantity)) {
      return;
    }

    contractService.requestContract(alert.getSymbol())
        .thenAccept(contractDetails -> {
          tradeService.trade(alert, strategy, contractDetails, existingQuantity);
        })
        .exceptionally(ex -> {
          log.error("Contract request failed for {}", alert.getSymbol(), ex);
          notificationService.notify("Contract request failed: " + ex.getMessage());
          return null;
        });
  }

  private boolean checkIfQuantityExecutable(AlertEntity alert, double existingQuantity) {
    if (Action.BUY.equals(alert.getAction()) && existingQuantity > 0) {
      logInfoAndNotify("Can't buy existing symbol=%s, peerSymbol=%s, action=%s, isPeer=%b",
          alert);
      return false;
    } else if (Action.SELL.equals(alert.getAction()) && existingQuantity <= 0) {
      logInfoAndNotify("Can't sell non existing symbol=%s, peerSymbol=%s, action=%s, isPeer=%b",
          alert);
      return false;
    }
    return true;
  }

  private void logInfoAndNotify(String template, AlertEntity alert) {
    String message =
        String.format(template, alert.getSymbol(), alert.getPeerSymbol(), alert.getAction(),
            alert.isPeer());
    log.info(message);
    notificationService.notify(message);
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
    if (strategy.getMaxPositionAmount().compareTo(alert.getHigh()) == -1) {
      log.error("Strategy: '{}'. MaxPositionAmount is less than price .", alert.getStrategy());
    }

    return optionalStrategy;
  }

}
