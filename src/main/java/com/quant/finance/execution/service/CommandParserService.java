package com.quant.finance.execution.service;

import com.ib.client.Types;
import com.quant.finance.execution.model.BotCommand;
import com.quant.finance.execution.model.TradeCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CommandParserService {

  public TradeCommand parse(String text, Long chatId) {
    if (text == null || text.isBlank()) {
      return unknown(text, chatId);
    }

    String[] parts = text.trim().split("\\s+");
    String cmd = parts[0].toLowerCase();

    return switch (cmd) {
      case "/positions" -> TradeCommand.builder()
          .command(BotCommand.POSITIONS)
          .chatId(chatId).rawText(text).build();

      case "/closeall" -> TradeCommand.builder()
          .command(BotCommand.CLOSE_ALL)
          .chatId(chatId).rawText(text).build();

      case "/buy" -> parseTrade(BotCommand.BUY, parts, chatId, text);
      case "/sell" -> parseTrade(BotCommand.SELL, parts, chatId, text);

      // /openorder BTCUSDT BUY 0.01 50000
      case "/openorder" -> {
        if (parts.length < 5) {
          yield unknown(text, chatId);
        }
        yield TradeCommand.builder()
            .command(BotCommand.OPEN_ORDER)
            .symbol(parts[1].toUpperCase())
            .action(Types.Action.get(parts[2].toUpperCase()))
            .quantity(Double.parseDouble(parts[3]))
            .limitPrice(Double.parseDouble(parts[4]))
            .chatId(chatId).rawText(text).build();
      }
      default -> unknown(text, chatId);
    };
  }

  // /buy BTCUSDT 0.01
  private TradeCommand parseTrade(BotCommand cmd, String[] parts, Long chatId, String raw) {
    if (parts.length < 3) {
      return unknown(raw, chatId);
    }
    return TradeCommand.builder()
        .command(cmd)
        .symbol(parts[1].toUpperCase())
        .quantity(Double.parseDouble(parts[2]))
        .chatId(chatId).rawText(raw).build();
  }

  private TradeCommand unknown(String text, Long chatId) {
    return TradeCommand.builder()
        .command(BotCommand.UNKNOWN)
        .rawText(text).chatId(chatId).build();
  }
}
