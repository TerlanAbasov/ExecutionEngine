package com.quant.finance.execution.service;

import com.quant.finance.execution.enums.BotCommand;
import com.quant.finance.execution.dto.TradeCommandDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CommandParserService {

  public TradeCommandDto parse(String text, Long chatId) {
    if (text == null || text.isBlank()) {
      return unknown(text, chatId);
    }

    String[] parts = text.trim().split("\\s+");
    String cmd = parts[0].toLowerCase();

    return switch (cmd) {
      case "/positions" -> new TradeCommandDto(BotCommand.POSITIONS, chatId, text);
      case "/pnl" -> new TradeCommandDto(BotCommand.PNL, chatId, text);
      case "/pnlsingle" -> parseTrade(BotCommand.PNL_SINGLE, parts, chatId, text);
      case "/accountsummary" -> new TradeCommandDto(BotCommand.ACCOUNT_SUMMARY, chatId, text);
      case "/accountupdates" -> new TradeCommandDto(BotCommand.ACCOUNT_UPDATES, chatId, text);
      case "/buy" -> parseTrade(BotCommand.BUY, parts, chatId, text);
      case "/sell" -> parseTrade(BotCommand.SELL, parts, chatId, text);
      case "/closeall" -> new TradeCommandDto(BotCommand.CLOSE_ALL, chatId, text);

      // /openorder BTCUSDT BUY 0.01 50000
      case "/openorders" -> parseTrade(BotCommand.OPEN_ORDERS, parts, chatId, text);
      //{
      //  if (parts.length < 5) {
      //    yield unknown(text, chatId);
      //  }
      //  yield TradeCommand.builder()
      //      .command(BotCommand.OPEN_ORDERS)
      //      .symbol(parts[1].toUpperCase())
      //      .action(Types.Action.get(parts[2].toUpperCase()))
      //      .quantity(Double.parseDouble(parts[3]))
      //      .limitPrice(Double.parseDouble(parts[4]))
      //      .chatId(chatId).rawText(text).build();
      //}
      case "/cancelorder" -> parseTrade(BotCommand.CANCEL_ORDER, parts, chatId, text);
      default -> unknown(text, chatId);
    };
  }

  // /buy BTCUSDT 0.01
  private TradeCommandDto parseTrade(BotCommand cmd, String[] parts, Long chatId, String raw) {
    if (parts.length < 2) {
      return unknown(raw, chatId);
    }

    //todo parse by command structure

    return TradeCommandDto.builder()
        .command(cmd)
        .identifier(parts[1].toUpperCase())
        .quantity(Double.parseDouble(parts[2]))
        .chatId(chatId).rawText(raw).build();
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
