package com.quant.finance.execution.service;

import com.ib.client.Contract;
import com.ib.client.ContractDetails;
import com.ib.client.Decimal;
import com.ib.client.Order;
import com.ib.client.OrderType;
import com.ib.client.Types;
import com.quant.finance.execution.client.IBClient;
import com.quant.finance.execution.dto.TradeCommandDto;
import com.quant.finance.execution.entity.AlertEntity;
import com.quant.finance.execution.entity.OrderEntity;
import com.quant.finance.execution.entity.StrategyEntity;
import com.quant.finance.execution.enums.AlertState;
import com.quant.finance.execution.model.Position;
import com.quant.finance.execution.repository.OrderRepository;
import com.quant.finance.execution.util.EngineUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockTradeExecutor implements TradeExecutor {
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
    return secType != Types.SecType.CRYPTO;
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

      if (parentOrder.action() == Types.Action.BUY) {
        placeBacketOrders(contractDetails, parentOrderEntity, parentOrder);
      }

      alertService.updateState(alert, AlertState.PROCESSED);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      notificationService.notify(String.format("TradeService.trade(). %s", e.getMessage()));
      alertService.updateStateAndDescription(alert, AlertState.PROCESSED, e.getMessage());
    }
  }

  private void placeBacketOrders(ContractDetails contractDetails, OrderEntity parentOrderEntity,
                                 Order parentOrder) {
    OrderEntity tpOrderEntity =
        orderService.buildAndSaveChildOrder(parentOrderEntity, OrderType.LMT, Types.Action.SELL);

    Order tpOrder = createChildOrder(tpOrderEntity, parentOrder, contractDetails);
    ibClient.placeOrder(contractDetails.contract(), tpOrder);

    OrderEntity slOrderEntity =
        orderService.buildAndSaveChildOrder(parentOrderEntity, OrderType.STP, Types.Action.SELL);

    Order slOrder = createChildOrder(slOrderEntity, parentOrder, contractDetails);
    ibClient.placeOrder(contractDetails.contract(), slOrder);
  }

  public Order createParentOrder(OrderEntity orderEntity, ContractDetails contractDetails) {
    Order order = baseOrder(orderEntity, contractDetails.contract());
    order.tif(Types.TimeInForce.DAY);
    setLimitOrAuxPrice(order, orderEntity, contractDetails);

    return order;
  }

  public Order createChildOrder(OrderEntity orderEntity, Order parent,
                                ContractDetails contractDetails) {
    Order order = baseOrder(orderEntity, contractDetails.contract());

    order.parentId(parent.orderId());
    order.tif(Types.TimeInForce.DAY);
    order.ocaGroup("BRACKET_" + parent.orderId());
    order.ocaType(1);
    order.totalQuantity(Decimal.get(orderEntity.getQuantity()));


    if (order.orderType() == OrderType.LMT) {
      order.lmtPrice(
          EngineUtil.snapToTick(orderEntity.getTakeProfitPrice().doubleValue(), contractDetails));
      order.transmit(false);
    } else if (order.orderType() == OrderType.STP) {
      order.auxPrice(
          EngineUtil.snapToTick(orderEntity.getStopLossPrice().doubleValue(), contractDetails));
      order.transmit(true);
    }

    return order;
  }

  private Order baseOrder(OrderEntity entity, Contract contract) {

    Order order = new Order();
    order.orderId(entity.getBrokerOrderId());
    order.action(entity.getAction().name());
    order.orderType(entity.getOrderType().name());
    order.totalQuantity(Decimal.get(entity.getQuantity()));

    return order;
  }

  private Order setLimitOrAuxPrice(Order order, OrderEntity orderEntity,
                                   ContractDetails contractDetails) {
    if (order.action() == Types.Action.BUY) {
      setLimitPrice(orderEntity, contractDetails, order);
      order.transmit(false);
    } else if (order.action() == Types.Action.SELL) {
      setAuxPrice(orderEntity, contractDetails, order);
      order.transmit(true);
    }

    return order;
  }

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