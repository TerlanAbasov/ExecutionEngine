package com.quant.finance.execution.service;

import com.quant.finance.execution.command.CommandDispatcher;
import com.quant.finance.execution.command.CommandParser;
import com.quant.finance.execution.config.ApplicationProperties;
import com.quant.finance.execution.dto.TradeCommandDto;
import com.quant.finance.execution.enums.BotCommand;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Service
@Profile("prod")
@RequiredArgsConstructor
@Slf4j
public class TelegramBotHandler
    implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {
  private final ApplicationProperties properties;
  private final CommandParser commandParser;
  private final CommandDispatcher commandDispatcher;
  private final TelegramClient telegramClient;

  @PostConstruct
  public void init() {
    log.info("TelegramBotHandler initialized!");

    //Update update = new Update();
    //Message message = new Message();
    //
    //message.setText("/buy RKLX MACTest BUY 100 MKT 100 DAY");
    //update.setMessage(message);
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
    log.info("\uD83D\uDCE9 '{}' command text received from chatId={}", text, chatId);

    TradeCommandDto cmd = commandParser.parse(text, chatId);

    if (cmd.getCommand() != BotCommand.UNKNOWN) {
      String response = commandDispatcher.dispatch(cmd);
      log.info(response);
    } else {
      log.error(cmd.toString());
    }
  }
}
