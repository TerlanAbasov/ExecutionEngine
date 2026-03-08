package com.quant.finance.execution.service;

import com.quant.finance.execution.client.IBClient;
import com.quant.finance.execution.config.ApplicationProperties;
import com.quant.finance.execution.dto.TradeCommandDto;
import com.quant.finance.execution.util.EngineUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommandDispatcher {
  private final IBClient iBClient;
  private final TradeService tradeService;
  private final ApplicationProperties properties;

  public String dispatch(TradeCommandDto cmd) {
    try {
      return switch (cmd.getCommand()) {
        case POSITIONS -> {
          iBClient.requestPositions();
          yield "📊 Open Positions will be sent";
        }
        case PNL -> {
          iBClient.requestPnl(EngineUtil.nextRequestId(), properties.getAccount().getId());
          yield "📊 PnL will be sent";
        }
        case PNL_SINGLE -> {
          yield "📊 PnL Single will be sent";
        }
        case ACCOUNT_SUMMARY -> {
          yield "📊 Account Summary will be sent";
        }
        case ACCOUNT_UPDATES -> {
          yield "📊 Account Updates will be sent";
        }
        case BUY -> {
          tradeService.buy(cmd.getIdentifier(), cmd.getQuantity());
          yield "📊 Symbol will be bought";
        }
        case SELL -> {
          tradeService.sell(cmd.getIdentifier(), cmd.getQuantity());
          yield "📊 Symbol will be sold";
        }
        case CLOSE_ALL -> {
          tradeService.closeAllPositions();
          yield "📊 Positions will be closed";
        }
        case OPEN_ORDERS -> {
          tradeService.placeOpenOrder(
              cmd.getIdentifier(), cmd.getAction(),
              cmd.getQuantity(), cmd.getLimitPrice());
          yield "📊 Open Orders will be sent";
        }
        case CANCEL_ORDER -> {
          yield "📊 Order will be cancelled";
        }
        case UNKNOWN -> " Unknown command : " + cmd.getCommand();
      };
    } catch (Exception e) {
      log.error("Error dispatching command: {}", cmd, e);
      return "❌ Error while executing command: " + e.getMessage();
    }
  }
}
