package com.quant.finance.execution.service;

import com.ib.client.Contract;
import com.ib.client.ContractDetails;
import com.ib.client.Decimal;
import com.ib.client.Order;
import com.ib.client.Types;
import com.quant.finance.execution.client.IBClient;
import com.quant.finance.execution.dto.TradeCommandDto;
import com.quant.finance.execution.entity.AlertEntity;
import com.quant.finance.execution.entity.OrderEntity;
import com.quant.finance.execution.entity.StrategyEntity;
import com.quant.finance.execution.enums.AlertState;
import com.quant.finance.execution.model.Position;
import com.quant.finance.execution.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CryptoTradeExecutor implements TradeExecutor {
  private final OrderRepository repository;
  private final OrderService orderService;
  private final NotificationService notificationService;
  private final EWrapperImpl eWrapper;
  private final AlertService alertService;
  @Lazy
  @Autowired
  private IBClient ibClient;

  @Override
  public boolean supports(Types.SecType secType) {
    return secType == Types.SecType.CRYPTO;
  }

  @Override
  @Transactional
  public void trade(AlertEntity alert,
                    StrategyEntity strategy,
                    ContractDetails contractDetails,
                    Position existingPosition) {
    try {
      OrderEntity parentOrderEntity =
          orderService.buildAndSaveParentOrder(alert, strategy, contractDetails, existingPosition);
      Order parentOrder = createParentOrder(parentOrderEntity, contractDetails);

      ibClient.placeOrder(contractDetails.contract(), parentOrder);
      alertService.updateState(alert, AlertState.PROCESSED);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      notificationService.notify(String.format("TradeService.trade(). %s", e.getMessage()));
      alertService.updateStateAndDescription(alert, AlertState.PROCESSED, e.getMessage());
    }
  }

  public Order createParentOrder(OrderEntity orderEntity, ContractDetails contractDetails) {
    Order order = baseOrder(orderEntity, contractDetails.contract());
    order.tif(Types.TimeInForce.IOC);
    order.totalQuantity(Decimal.ZERO);
    order.cashQty(orderEntity.getQuantity()); // or calculated USD amount
    setLimitOrAuxPrice(order, orderEntity, contractDetails);

    //if (orderEntity.getOrderType() == OrderType.MKT) {
    //  order.tif(Types.TimeInForce.IOC);
    //} else {
    //  order.tif(Types.TimeInForce.Minutes);
    //}

    return order;
  }

  private Order baseOrder(OrderEntity entity, Contract contract) {
    Order order = new Order();
    order.orderId(entity.getBrokerOrderId());
    order.action(entity.getAction().name());
    order.orderType(entity.getOrderType().name());

    return order;
  }

  private Order setLimitOrAuxPrice(Order order, OrderEntity orderEntity,
                                   ContractDetails contractDetails) {
    if (order.action() == Types.Action.BUY) {
      setLimitPrice(orderEntity, contractDetails, order);
    } else if (order.action() == Types.Action.SELL) {
      setAuxPrice(orderEntity, contractDetails, order);
    }

    order.transmit(true);//for tp and sl it must be true

    return order;
  }

  //todo add TP and STP_LMT(not STP) orders after parent FILLED

  /**
   * if (orderType == STP_LMT) {
   * order.auxPrice(stopPrice);
   * <p>
   * // crypto üçün tighter spread
   * order.lmtPrice(stopPrice - slippageBuffer);
   * }
   **/

  @Override
  public void sell(TradeCommandDto commandDto) {

  }

  @Override
  public void buy(TradeCommandDto commandDto) {

  }

  @Override
  public void closeAllPositions(TradeCommandDto commandDto) {

  }
}
