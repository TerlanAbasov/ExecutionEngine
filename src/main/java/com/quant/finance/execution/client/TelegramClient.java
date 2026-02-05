package com.quant.finance.execution.client;

import com.quant.finance.execution.model.DiscordMessage;
import com.quant.finance.execution.model.TelegramRequest;
import com.quant.finance.execution.model.TelegramResponse;
import feign.Headers;
import feign.Logger;
import feign.Param;
import feign.RequestLine;
import feign.codec.ErrorDecoder;
import feign.error.AnnotationErrorDecoder;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "telegram-client",
    url = "${application.client.telegram.url}",
    configuration = TelegramClient.FeignConfiguration.class)
public interface TelegramClient {

  @PostMapping("/bot8068983143:AAGyxjjqig8ZJAjBFuxdg8Obwy-Y41OKCdA/sendMessage")
  @Headers("Content-Type: application/json")
  TelegramResponse sendMessage(@Param("token") String token, @RequestParam("chat_id") String chatId,
                               @RequestParam("text") String text);

  class FeignConfiguration {
    @Bean
    Logger.Level feignLoggerLevel() {
      return Logger.Level.FULL;
    }

    @Bean
    public ErrorDecoder feignErrorDecoder() {
      return AnnotationErrorDecoder
          .builderFor(DiscordClient.class)
          .build();
    }
  }
}
