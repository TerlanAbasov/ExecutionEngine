package com.quant.finance.execution.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quant.finance.execution.config.ApplicationProperties;
import com.quant.finance.execution.dto.TVAlertDto;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {
  private final TelegramClient telegramClient;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final ApplicationProperties properties;

  public void notify(TVAlertDto alert) {

    if (properties.getClient().getTelegram().isEnabled()) {
      //telegramClient.sendMessage("8068983143:AAGyxjjqig8ZJAjBFuxdg8Obwy-Y41OKCdA", "1014578999",
      //    message);

      try {
        String message = convertToJson(alert);
        log.debug("Sending notification: {}", message);

        SendMessage msg = SendMessage.builder()
            .chatId(properties.getClient().getTelegram().getChatId())
            .text(message)
            .parseMode("Markdown")
            .build();
        telegramClient.execute(msg);
      } catch (Exception e) {
        log.error(e.getMessage(), e);
      }
    }
  }

  public void notify(String message) {
    if (properties.getClient().getTelegram().isEnabled()) {
      //telegramClient.sendMessage("8068983143:AAGyxjjqig8ZJAjBFuxdg8Obwy-Y41OKCdA", "1014578999",
      //    message);

      log.debug("Sending notification: {}", message);

      SendMessage msg = SendMessage.builder()
          .chatId(properties.getClient().getTelegram().getChatId())
          .text(message)
          .parseMode("Markdown")
          .build();
      try {
        telegramClient.execute(msg);
      } catch (Exception e) {
        log.error(e.getMessage(), e);
      }
    }
  }

  public String convertToJson(TVAlertDto model) throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(model);
  }

  @PreDestroy
  public void onShutdown() {
    notify("\uD83D\uDED1 Shutting down ExecutionEngine!");
  }
}

