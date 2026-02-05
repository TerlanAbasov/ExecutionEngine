package com.quant.finance.execution.client;

import com.quant.finance.execution.model.DiscordMessage;
import feign.Logger;
import feign.codec.ErrorDecoder;
import feign.error.AnnotationErrorDecoder;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "discord-client",
    url = "${application.client.discord.url}",
    configuration = DiscordClient.FeignConfiguration.class)
public interface DiscordClient {

  @PostMapping("/api/webhooks/1466818290696585348/VAbCpx_fMn1JDnASswamPblRMbOiCOJ-bEe648EZ605xsgX8SDbfC_MLeo5mV45BtA6T")
  void notify(@RequestBody DiscordMessage message);

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