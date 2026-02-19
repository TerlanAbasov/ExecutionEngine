package com.quant.finance.execution.service;

import com.ib.client.Contract;
import com.ib.client.Decimal;
import com.ib.client.Order;
import com.ib.client.OrderStatus;
import com.ib.client.OrderType;
import com.quant.finance.execution.client.IBClient;
import com.quant.finance.execution.config.ApplicationProperties;
import com.quant.finance.execution.dto.TVAlertDto;
import com.quant.finance.execution.entity.AlertEntity;
import com.quant.finance.execution.entity.OrderEntity;
import com.quant.finance.execution.entity.StrategyEntity;
import com.quant.finance.execution.enums.OrderAction;
import com.quant.finance.execution.model.ContractData;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExecutionService {

  private final NotificationService notificationService;
  private final IBClient ibClient;
  private final ApplicationProperties properties;
  private final StrategyService strategyService;
  private final TradeService tradeService;
  private final EWrapperImpl eWrapper;
  private final ContractService contractService;
  private final PositionService positionService;
  private final AlertService alertService;

  public void executeStrategy(TVAlertDto tvAlertDto) {
    notificationService.notify(tvAlertDto);
    AlertEntity alert = alertService.save(tvAlertDto);

    Optional<StrategyEntity> strategy = checkStrategy(alert.getStrategy());
    if (strategy.isEmpty()) {
      return;
    }

    //ibClient.getMarketData(contract);
    //ibClient.getContractDetails(IBClient.getNextOrderId(), contract);
    //eWrapper.getEClientSocket().reqPositions();
    //ibClient.placeOrder(contract, order);


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

  /*
  private void checkBeforeOrder(AlertEntity alert) {
    Map<String, Decimal> positions = positionService.requestPositions().get();

    Decimal quantity = positions.getOrDefault(alert.getSymbol(), Decimal.ZERO);

    if (quantity.compareTo(Decimal.ZERO) > 0) {
      log.warn("Already holding symbol: {}, quantity: {}", orderEntity.getSymbol(), quantity);
      return;
    }

    contractService.requestContract(alert.getSymbol())
        .thenAccept(contract -> {
          orderService.placeOrder(orderEntity, contract);
        });
  }
   */

  public Optional<StrategyEntity> checkStrategy(String strategyName) {
    Optional<StrategyEntity> optionalStrategy = strategyService.findStrategyByName(strategyName);

    if (optionalStrategy.isEmpty()) {
      log.error("Strategy: '{}' does not exits.", strategyName);
      notificationService.notify(String.format("Strategy: '%s' does not exits.", strategyName));
    }

    return optionalStrategy;
  }

  public void makeMainOrder() {
  }

  public void makeStopOrder() {
  }

  public void makeTakeProfitOrder() {
    // TODO: 09.02.26 need analysis
  }

  private Contract createContract(OrderEntity orderEntity) {
    Contract contract = new Contract();
    contract.secType("STK");
    contract.currency(orderEntity.getCurrency());
    contract.exchange("SMART");

    return contract;
  }

  private Order createOrder(OrderEntity orderEntity) {
    Order order = new Order();
    order.orderType(orderEntity.getOrderType());
    order.totalQuantity(Decimal.get(orderEntity.getQuantity()));

    return order;
  }

  private BigDecimal calculateLimitPrice() {
    return null;
  }

  private BigDecimal calculateStopPrice() {
    return null;
  }
}
