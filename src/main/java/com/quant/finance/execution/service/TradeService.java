package com.quant.finance.execution.service;

import com.ib.client.ContractDetails;
import com.ib.client.Types;
import com.quant.finance.execution.dto.TradeCommandDto;
import com.quant.finance.execution.entity.AlertEntity;
import com.quant.finance.execution.entity.StrategyEntity;
import com.quant.finance.execution.model.Position;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TradeService {
  @Lazy
  @Autowired
  private TradeExecutorFactory factory;
  @Lazy
  @Autowired
  private StrategyService strategyService;

  public void trade(AlertEntity alert,
                    StrategyEntity strategy,
                    ContractDetails contractDetails,
                    Position existingPosition) {

    TradeExecutor executor = factory.resolve(contractDetails.contract().secType());
    executor.trade(alert, strategy, contractDetails, existingPosition);
  }

  public void trade(TradeCommandDto tradeCommandDto,
                    StrategyEntity strategy,
                    ContractDetails contractDetails,
                    Position existingPosition) {

    TradeExecutor executor = factory.resolve(contractDetails.contract().secType());
    executor.trade(tradeCommandDto, strategy, contractDetails, existingPosition);
  }

  public void buy(TradeCommandDto commandDto) {
    strategyService.executeStrategy(commandDto);
/*    Types.SecType secType = Types.SecType.STK;

    TradeExecutor executor = factory.resolve(secType);
    executor.buy(commandDto)*/
    ;
  }

  public void sell(TradeCommandDto commandDto) {
    strategyService.executeStrategy(commandDto);
    /*Types.SecType secType = Types.SecType.STK;

    TradeExecutor executor = factory.resolve(secType);
    executor.sell(commandDto);*/
  }

  public void closeAllPositions(TradeCommandDto commandDto) {
    //todo get sec type from command
    Types.SecType secType = Types.SecType.STK;

    TradeExecutor executor = factory.resolve(secType);
    executor.closeAllPositions(commandDto);
  }
}