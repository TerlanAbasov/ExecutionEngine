package com.quant.finance.execution.service;

import com.ib.client.ContractDetails;
import com.ib.client.Types;
import com.quant.finance.execution.dto.TradeCommandDto;
import com.quant.finance.execution.entity.AlertEntity;
import com.quant.finance.execution.entity.StrategyEntity;
import com.quant.finance.execution.model.Position;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TradeService {

  private final TradeExecutorFactory factory;

  public void trade(AlertEntity alert,
                    StrategyEntity strategy,
                    ContractDetails contractDetails,
                    Position existingPosition) {

    TradeExecutor executor = factory.resolve(contractDetails.contract().secType());
    executor.trade(alert, strategy, contractDetails, existingPosition);
  }

  public void buy(TradeCommandDto commandDto) {
    //todo get sec type from command
    Types.SecType secType = Types.SecType.STK;

    TradeExecutor executor = factory.resolve(secType);
    executor.buy(commandDto);

  }

  public void sell(TradeCommandDto commandDto) {
    //todo get sec type from command
    Types.SecType secType = Types.SecType.STK;

    TradeExecutor executor = factory.resolve(secType);
    executor.sell(commandDto);
  }

  public void closeAllPositions(TradeCommandDto commandDto) {
    //todo get sec type from command
    Types.SecType secType = Types.SecType.STK;

    TradeExecutor executor = factory.resolve(secType);
    executor.closeAllPositions(commandDto);
  }
}