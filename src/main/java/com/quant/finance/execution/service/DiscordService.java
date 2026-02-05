package com.quant.finance.execution.service;

import com.quant.finance.execution.client.DiscordClient;
import com.quant.finance.execution.model.DiscordMessage;
import java.io.IOException;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class DiscordService {

  private final DiscordClient discordClient;

  public void sendNotification(DiscordMessage message) {
  }

  private static class JSONObject {

    private final HashMap<String, Object> map = new HashMap<>();

    void put(String key, Object value) {
      if (value != null) {
        map.put(key, value);
      }
    }
  }
}
