package com.quant.finance.execution.command;

import com.quant.finance.execution.client.IBClient;
import com.quant.finance.execution.config.ApplicationProperties;
import com.quant.finance.execution.dto.TradeCommandDto;
import com.quant.finance.execution.service.EngineService;
import com.quant.finance.execution.service.NotificationService;
import com.quant.finance.execution.service.OrderService;
import com.quant.finance.execution.service.PositionService;
import com.quant.finance.execution.service.TradeService;
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
  private final OrderService orderService;
  private final NotificationService notificationService;
  private final PositionService positionService;

  public String dispatch(TradeCommandDto commandDto) {
    try {
      return switch (commandDto.getCommand()) {
        case POSITIONS -> {
          positionService.sendPositions();
          yield "📊 Open Positions will be sent";
        }
        case SYNC_POSITIONS -> {
          positionService.syncronizePositions();
          yield "📊 Open Positions will be sent";
        }
        case PNL -> {
          iBClient.requestPnl();
          yield "📊 PnL will be sent";
        }
        case PNL_SINGLE -> {
          iBClient.requestSinglePnl(Integer.parseInt(commandDto.getIdentifier()));
          yield "📊 PnL Single will be sent";
        }
        case BUY -> {
          tradeService.buy(commandDto);
          yield "📊 Symbol will be bought";
        }
        case SELL -> {
          tradeService.sell(commandDto);
          yield "📊 Symbol will be sold";
        }
        case CLOSE_ALL -> {
          tradeService.closeAllPositions(commandDto);
          yield "📊 Positions will be closed";
        }
        case START_ACCOUNT_SUMMARY -> {
          iBClient.requestAccountSummary();
          yield "📊 Account Summary will be sent";
        }
        case STOP_ACCOUNT_SUMMARY -> {
          iBClient.cancelAccountSummary();
          yield "📊 Account Summary will be sent";
        }
        case START_ACCOUNT_UPDATES -> {
          iBClient.requestAccountUpdates();
          yield "📊 Account Updates started";
        }
        case STOP_ACCOUNT_UPDATES -> {
          iBClient.cancelAccountUpdates();
          yield "📊 Account Updates stopped";
        }
        case OPEN_ORDERS -> {
          iBClient.requestOpenOrders();
          yield "📊 Open Orders will be sent";
        }
        case CANCEL_ORDER -> {
          orderService.cancelOrderBy(commandDto.getIdentifier());
          yield "📊 Order will be cancelled";
        }
        case CANCEL_OPEN_ORDERS -> {
          orderService.cancelOpenOrders();
          yield "📊 Open Orders will be cancelled";
        }
        case RESTART_ENGINE -> {
          EngineService.restartEngine();
          yield "Restarting Engine...";
        }
        case STOP_ENGINE -> {
          EngineService.stopEngine();
          yield "Stopping Engine...";
        }
        case START_ENGINE -> {
          EngineService.startEngine();
          yield "Starting Engine...";
        }
        case UNKNOWN -> {
          String message = "Unknown command : " + commandDto.getCommand();
          log.warn(message);
          notificationService.notify(message);
          yield message;
        }
      };
    } catch (Exception e) {
      log.error("Error dispatching command={}", commandDto, e);
      return "❌ Error while executing command: " + e.getMessage();
    }
  }
}
