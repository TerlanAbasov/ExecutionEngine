package com.quant.finance.execution.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quant.finance.execution.client.TelegramClient;
import com.quant.finance.execution.config.ApplicationProperties;
import com.quant.finance.execution.dto.TVAlertDto;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {
  private final TelegramClient telegramClient;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final ApplicationProperties properties;

  public void notify(TVAlertDto alert) {
    try {
      String message = convertToJson(alert);
      log.debug("Sending notification: {}", message);

      if (properties.getClient().getTelegram().isEnabled()) {
        telegramClient.sendMessage("8068983143:AAGyxjjqig8ZJAjBFuxdg8Obwy-Y41OKCdA", "1014578999",
            message);
      }
    } catch (JsonProcessingException e) {
      log.error(e.getMessage(), e);
    }
  }

  public void notify(String message) {
    log.debug("Sending notification: {}", message);

    if (properties.getClient().getTelegram().isEnabled()) {
      telegramClient.sendMessage("8068983143:AAGyxjjqig8ZJAjBFuxdg8Obwy-Y41OKCdA", "1014578999",
          message);
    }
  }

  public String convertToJson(TVAlertDto model) throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(model);
  }

  @PreDestroy
  public void onShutdown() {
    notify("Shutting down ExecutionEngine!");
  }
}
