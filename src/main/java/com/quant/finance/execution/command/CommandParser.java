package com.quant.finance.execution.command;

import com.ib.client.OrderType;
import com.ib.client.Types;
import com.quant.finance.execution.dto.TradeCommandDto;
import com.quant.finance.execution.enums.BotCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CommandParser {

  public TradeCommandDto parse(String text, Long chatId) {
    if (text == null || text.isBlank()) {
      return unknown(text, chatId);
    }

    String[] parts = text.trim().split("\\s+");
    String cmd = parts[0].toLowerCase();

    return switch (cmd) {
      case "/positions" -> new TradeCommandDto(BotCommand.POSITIONS, chatId, text);
      case "/pnl" -> new TradeCommandDto(BotCommand.PNL, chatId, text);
      case "/pnlsingle" -> parseCommandText(BotCommand.PNL_SINGLE, parts, chatId, text);
      case "/buy" -> parseCommandText(BotCommand.BUY, parts, chatId, text);
      case "/sell" -> parseCommandText(BotCommand.SELL, parts, chatId, text);
      case "/closeall" -> new TradeCommandDto(BotCommand.CLOSE_ALL, chatId, text);
      case "/startaccountsummary" ->
          new TradeCommandDto(BotCommand.START_ACCOUNT_SUMMARY, chatId, text);
      case "/stopaccountsummary" ->
          new TradeCommandDto(BotCommand.STOP_ACCOUNT_SUMMARY, chatId, text);
      case "/startaccountupdates" ->
          new TradeCommandDto(BotCommand.START_ACCOUNT_UPDATES, chatId, text);
      case "/stopaccountupdates" ->
          new TradeCommandDto(BotCommand.STOP_ACCOUNT_UPDATES, chatId, text);
      case "/openorders" -> new TradeCommandDto(BotCommand.OPEN_ORDERS, chatId, text);
      case "/cancelorder" -> parseCommandText(BotCommand.CANCEL_ORDER, parts, chatId, text);
      case "/cancelopenorders" -> new TradeCommandDto(BotCommand.CANCEL_OPEN_ORDERS, chatId, text);
      case "/restartengine" -> new TradeCommandDto(BotCommand.RESTART_ENGINE, chatId, text);
      case "/stopengine" -> new TradeCommandDto(BotCommand.STOP_ENGINE, chatId, text);
      case "/startengine" -> new TradeCommandDto(BotCommand.START_ENGINE, chatId, text);
      default -> unknown(text, chatId);
    };
  }

  // /buy BTCUSDT 0.01
  private TradeCommandDto parseCommandText(BotCommand cmd, String[] parts, Long chatId,
                                           String raw) {
    if (parts.length < 2) {
      return unknown(raw, chatId);
    }

    TradeCommandDto dto = new TradeCommandDto();

    if (parts.length == 2) {
      dto.setIdentifier(parts[1]);
    }
    if (parts.length == 3) {
      dto.setStrategy(parts[2]);
    }
    if (parts.length == 4) {
      dto.setAction(Types.Action.valueOf(parts[3]));
    }
    if (parts.length == 5) {
      dto.setQuantity(Double.valueOf(parts[4]));
    }
    if (parts.length == 6) {
      dto.setOrderType(OrderType.valueOf(parts[5]));
    }
    if (parts.length == 7) {
      dto.setLimitPrice(Double.valueOf(parts[6]));
    }
    if (parts.length == 8) {
      dto.setTif(Types.TimeInForce.valueOf(parts[7]));
    }

    return dto;
  }

  private TradeCommandDto unknown(String text, Long chatId) {
    return TradeCommandDto.builder()
        .command(BotCommand.UNKNOWN)
        .rawText(text).chatId(chatId).build();
  }

  public static Integer getValue(int[] arr, int index) {
    if (arr.length >= arr.length) {
      return null;
    }
    return arr[index];
  }
}
