package com.quant.finance.execution.service;

import com.ib.client.ContractDetails;
import com.ib.client.Decimal;
import com.ib.client.Order;
import com.ib.client.OrderType;
import com.ib.client.Types.Action;
import com.ib.client.Types.TimeInForce;
import com.quant.finance.execution.client.IBClient;
import com.quant.finance.execution.dto.TradeCommandDto;
import com.quant.finance.execution.entity.AlertEntity;
import com.quant.finance.execution.entity.OrderEntity;
import com.quant.finance.execution.entity.StrategyEntity;
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
public class TradeService {
  private final OrderRepository repository;
  private final OrderService orderService;
  private final NotificationService notificationService;
  private final EWrapperImpl eWrapper;
  @Lazy
  @Autowired
  private IBClient ibClient;

  @Transactional
  public void trade(AlertEntity alert, StrategyEntity strategy, ContractDetails contractDetails,
                    double existingQuantity) {
    try {
      OrderEntity parentOrderEntity =
          orderService.buildAndSaveParentOrder(alert, strategy, contractDetails, existingQuantity);
      Order parentOrder = createParentOrder(parentOrderEntity, contractDetails);
      ibClient.placeOrder(contractDetails.contract(), parentOrder);

      if (parentOrder.action() == Action.BUY) {
        pleaceBacketOrders(contractDetails, parentOrderEntity, parentOrder);
      }
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      notificationService.notify(String.format("TradeService.trade(). %s", e.getMessage()));
    }
  }

  private void pleaceBacketOrders(ContractDetails contractDetails, OrderEntity parentOrderEntity,
                                  Order parentOrder) {
    OrderEntity tpOrderEntity =
        orderService.buildAndSaveChildOrder(parentOrderEntity, OrderType.LMT, Action.SELL);

    Order tpOrder = createChildOrder(tpOrderEntity, parentOrder, contractDetails);
    ibClient.placeOrder(contractDetails.contract(), tpOrder);

    OrderEntity slOrderEntity =
        orderService.buildAndSaveChildOrder(parentOrderEntity, OrderType.STP, Action.SELL);

    Order slOrder = createChildOrder(slOrderEntity, parentOrder, contractDetails);
    ibClient.placeOrder(contractDetails.contract(), slOrder);
  }

  public Order createParentOrder(OrderEntity orderEntity, ContractDetails contractDetails) {
    Order order = baseOrder(orderEntity);
    order.tif(TimeInForce.DAY);

    if (order.action() == Action.BUY) {
      setLimitPrice(orderEntity, contractDetails, order);
      order.transmit(false);
    } else if (order.action() == Action.SELL) {
      setAuxPrice(orderEntity, contractDetails, order);
      order.transmit(true);
    }

    return order;
  }

  private void setAuxPrice(OrderEntity orderEntity, ContractDetails contractDetails, Order order) {
    if (order.orderType() == OrderType.LMT) {
      order.auxPrice(snapToTick(orderEntity.getLimitPrice().doubleValue(), contractDetails));
    }
  }

  private void setLimitPrice(OrderEntity orderEntity, ContractDetails contractDetails,
                             Order order) {
    if (order.orderType() == OrderType.LMT) {
      double limitPrice = orderEntity.getLimitPrice().doubleValue();
      order.lmtPrice(snapToTick(orderEntity.getLimitPrice().doubleValue(), contractDetails));
    }
  }

  public Order createChildOrder(OrderEntity orderEntity, Order parent,
                                ContractDetails contractDetails) {
    Order order = baseOrder(orderEntity);

    order.parentId(parent.orderId());
    order.tif(TimeInForce.GTC);
    order.ocaGroup("BRACKET_" + parent.orderId());
    order.ocaType(1);

    if (order.orderType() == OrderType.LMT) {
      order.lmtPrice(snapToTick(orderEntity.getTakeProfitPrice().doubleValue(), contractDetails));
      order.transmit(false);
    } else if (order.orderType() == OrderType.STP) {
      order.auxPrice(snapToTick(orderEntity.getStopLossPrice().doubleValue(), contractDetails));
      order.transmit(true);
    }

    return order;
  }

  private Order baseOrder(OrderEntity entity) {

    Order order = new Order();
    order.orderId(entity.getBrokerOrderId());
    order.action(entity.getAction().name());
    order.orderType(entity.getOrderType().name());
    order.totalQuantity(Decimal.get(entity.getQuantity()));

    return order;
  }

  private double snapToTick(double price, ContractDetails contractDetails) {
    double tick = contractDetails.minTick();
    return Math.round(price / tick) * tick;
  }

  public void buy(TradeCommandDto command) {
    //todo
  }

  public void sell(TradeCommandDto command) {
    //todo
  }

  public void closeAllPositions() {
    //todo
  }
}
