package com.quant.finance.execution.service;

import com.ib.client.ContractDetails;
import com.ib.client.Order;
import com.ib.client.OrderType;
import com.ib.client.Types;
import com.quant.finance.execution.dto.TradeCommandDto;
import com.quant.finance.execution.entity.AlertEntity;
import com.quant.finance.execution.entity.OrderEntity;
import com.quant.finance.execution.entity.StrategyEntity;
import com.quant.finance.execution.model.Position;
import com.quant.finance.execution.util.EngineUtil;

public interface TradeExecutor {
  boolean supports(Types.SecType secType);

  void trade(AlertEntity alert,
             StrategyEntity strategy,
             ContractDetails contractDetails,
             Position existingPosition);

  void buy(TradeCommandDto commandDto);

  void sell(TradeCommandDto commandDto);

  void closeAllPositions(TradeCommandDto commandDto);


  default void setAuxPrice(OrderEntity orderEntity, ContractDetails contractDetails, Order order) {
    if (order.orderType() == OrderType.LMT) {
      order.auxPrice(
          EngineUtil.snapToTick(orderEntity.getLimitPrice().doubleValue(), contractDetails));
    }
  }

  default void setLimitPrice(OrderEntity orderEntity, ContractDetails contractDetails,
                             Order order) {
    if (order.orderType() == OrderType.LMT) {
      double limitPrice = orderEntity.getLimitPrice().doubleValue();
      order.lmtPrice(
          EngineUtil.snapToTick(orderEntity.getLimitPrice().doubleValue(), contractDetails));
    }
  }
}
