package com.quant.finance.execution.service;

import com.ib.client.Contract;
import com.ib.client.Decimal;
import com.ib.client.Order;
import com.ib.client.OrderCancel;
import com.ib.client.OrderStatus;
import com.ib.client.OrderType;
import com.ib.client.Types.Action;
import com.ib.client.Types.TimeInForce;
import com.quant.finance.execution.client.IBClient;
import com.quant.finance.execution.dto.OrderCancelDto;
import com.quant.finance.execution.entity.AlertEntity;
import com.quant.finance.execution.entity.OrderEntity;
import com.quant.finance.execution.entity.StrategyEntity;
import com.quant.finance.execution.model.OrderResult;
import com.quant.finance.execution.repository.OrderRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
  private final EWrapperImpl eWrapper;
  @Lazy
  @Autowired
  private IBClient ibClient;

  @Transactional
  public void trade(AlertEntity alert, StrategyEntity strategy, Contract contract,
                    double existingQuantity) {
    OrderEntity parentOrderEntity =
        orderService.buildAndSaveParentOrder(alert, strategy, contract, existingQuantity);
    Order parentOrder = createParentOrder(parentOrderEntity);
    ibClient.placeOrder(contract, parentOrder);

    if (parentOrder.action() == Action.BUY) {
      OrderEntity tpOrderEntity =
          orderService.buildAndSaveChildOrder(parentOrderEntity, OrderType.LMT, Action.SELL);
      Order tpOrder = createChildOrder(tpOrderEntity, parentOrder, false);
      ibClient.placeOrder(contract, tpOrder);

      OrderEntity slOrderEntity =
          orderService.buildAndSaveChildOrder(parentOrderEntity, OrderType.STP, Action.SELL);
      Order slOrder = createChildOrder(slOrderEntity, parentOrder, true);
      ibClient.placeOrder(contract, slOrder);
    }
  }

  public Order createParentOrder(OrderEntity orderEntity) {
    Order order = new Order();
    setOrderId(orderEntity, order);
    order.action(orderEntity.getAction().name());
    order.orderType(orderEntity.getOrderType().name());
    order.totalQuantity(Decimal.get(orderEntity.getQuantity()));
    order.tif(TimeInForce.DAY);

    if (order.action() == Action.BUY) {
      order.lmtPrice(orderEntity.getLimitPrice().doubleValue());
      order.transmit(false);
    } else if (order.action() == Action.SELL) {
      order.transmit(true);

      if (order.orderType() == OrderType.LMT) {
        order.auxPrice(orderEntity.getLimitPrice().doubleValue());
      }
    }

    return order;
  }

  public Order createChildOrder(OrderEntity orderEntity, Order parent, boolean transmit) {
    String ocaGroup = "BRACKET_" + parent.orderId();

    Order order = new Order();
    order.parentId(parent.orderId());
    setOrderId(orderEntity, order);
    order.action(orderEntity.getAction().name());
    order.orderType(orderEntity.getOrderType().name());
    order.totalQuantity(Decimal.get(orderEntity.getQuantity()));
    order.tif(TimeInForce.GTC);
    order.ocaGroup(ocaGroup);
    order.ocaType(1);
    order.transmit(true);

    if (order.orderType() == OrderType.LMT) {
      order.lmtPrice(orderEntity.getTakeProfitPrice().doubleValue());
    } else if (order.orderType() == OrderType.STP) {
      order.auxPrice(orderEntity.getStopLossPrice().doubleValue());
    }
    return order;
  }

  private void setOrderId(OrderEntity orderEntity, Order order) {
    order.orderId(Integer.parseInt(orderEntity.getBrokerOrderId()));
  }

  public void cancelOrder(OrderCancelDto dto) {
    if (StringUtils.isBlank(dto.getOrderId())) {
      orderService.findCancellableOrdersBySymbol(dto.getSymbol()).forEach(this::cancelIfIsActive);
    } else {
      Optional<OrderEntity> order = orderService.findByBrokerOrderId(dto.getOrderId());
      if (order.isPresent()) {
        cancelIfIsActive(order.get());
      }
    }
  }

  private void cancelIfIsActive(OrderEntity order) {
    if (order.getStatus().isActive() || order.getStatus() == OrderStatus.ApiPending) {
      log.info("Cancelling order with id: {}, status: {}", order.getBrokerOrderId(),
          order.getStatus());
      ibClient.cancelOrder(Integer.parseInt(order.getBrokerOrderId()), new OrderCancel());
    } else {
      log.info("Unable to cancel order with id: {}, status: {}", order.getBrokerOrderId(),
          order.getStatus());
    }
  }

  public OrderResult buy(String symbol, Double quantity) {
    log.info("Engine: BUY {} {}", quantity, symbol);
    // TODO: call your engine
    return OrderResult.builder()
        .success(true).symbol(symbol).side(Action.BUY)
        .quantity(quantity).orderId("ORD-001")
        .message("Buy order placed successfully").build();
  }

  public OrderResult sell(String symbol, Double quantity) {
    log.info("Engine: SELL {} {}", quantity, symbol);
    return OrderResult.builder()
        .success(true).symbol(symbol).side(Action.SELL)
        .quantity(quantity).orderId("ORD-002")
        .message("Sell order placed successfully").build();
  }

  public OrderResult closeAllPositions() {
    log.info("Engine: CLOSE ALL");
    return OrderResult.builder()
        .success(true).message("All positions closed").build();
  }

  public OrderResult placeOpenOrder(String symbol, Action action, Double quantity, Double price) {
    log.info("Engine: OPEN ORDER {} {} {} @ {}", action.name(), quantity, symbol, price);
    return OrderResult.builder()
        .success(true).symbol(symbol).side(action)
        .quantity(quantity).price(price).orderId("ORD-003")
        .message("Open order placed successfully").build();
  }

}
