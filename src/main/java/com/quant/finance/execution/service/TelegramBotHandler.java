package com.quant.finance.execution.service;

import com.quant.finance.execution.client.IBClient;
import com.quant.finance.execution.config.ApplicationProperties;
import com.quant.finance.execution.model.TradeCommand;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Service
@Profile("prod")
@RequiredArgsConstructor
@Slf4j
public class TelegramBotHandler
    implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {
  private final ApplicationProperties properties;
  private final CommandParserService parserService;
  private final TradeService tradeService;
  private final IBClient iBClient;
  private final NotificationFormatterService formatterService;
  private final TelegramClient telegramClient;

  @PostConstruct
  public void init() {
    log.info("TelegramBotHandler initialized!");
  }

  @Override
  public String getBotToken() {
    return properties.getClient().getTelegram().getToken();
  }

  @Override
  public LongPollingSingleThreadUpdateConsumer getUpdatesConsumer() {
    return this;
  }

  @Override
  public void consume(Update update) {
    if (!update.hasMessage() || !update.getMessage().hasText()) {
      return;
    }

    Long chatId = update.getMessage().getChatId();
    String text = update.getMessage().getText();
    log.info("\uD83D\uDCE9 '{}' command received from chatId: {}", text, chatId);

    TradeCommand cmd = parserService.parse(text, chatId);
    String response = dispatch(cmd);
    //sendMessage(chatId, response);
  }

  private String dispatch(TradeCommand cmd) {
    try {
      return switch (cmd.getCommand()) {
        case POSITIONS -> {
          iBClient.requestPositions();
          yield "📊 Open Positions will be sent";
        }
        case BUY -> formatterService.formatOrderResult(
            tradeService.buy(cmd.getSymbol(), cmd.getQuantity()));
        case SELL -> formatterService.formatOrderResult(
            tradeService.sell(cmd.getSymbol(), cmd.getQuantity()));
        case CLOSE_ALL -> formatterService.formatOrderResult(
            tradeService.closeAllPositions());
        case OPEN_ORDER -> formatterService.formatOrderResult(
            tradeService.placeOpenOrder(
                cmd.getSymbol(), cmd.getAction(),
                cmd.getQuantity(), cmd.getLimitPrice()));
        case UNKNOWN -> "❓ Unknown command. Try:\n" +
            "/positions\n/buy BTCUSDT 0.01\n/sell BTCUSDT 0.01\n" +
            "/closeall\n/openorder BTCUSDT BUY 0.01 50000";
      };
    } catch (Exception e) {
      log.error("Error dispatching command: {}", cmd, e);
      return "❌ Error executing command: " + e.getMessage();
    }
  }

  public void sendMessage(Long chatId, String text) {
    SendMessage msg = SendMessage.builder()
        .chatId(chatId.toString())
        .text(text)
        .parseMode("Markdown")
        .build();
    try {
      telegramClient.execute(msg);
    } catch (TelegramApiException e) {
      log.error("Failed to send message", e);
    }
  }

  public void notify(String text) {
    sendMessage(Long.valueOf(properties.getClient().getTelegram().getChatId()), text);
  }

}
