package com.quant.finance.execution.client;

import com.quant.finance.execution.dto.TVAlertDto;
import com.quant.finance.execution.model.TelegramResponse;
import feign.Headers;
import feign.Logger;
import feign.Param;
import feign.codec.ErrorDecoder;
import feign.error.AnnotationErrorDecoder;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "routing-client",
    url = "${application.client.route.url}",
    configuration = RoutingClient.FeignConfiguration.class)
public interface RoutingClient {

  @PostMapping
  @Headers("Content-Type: application/json")
  void routeAlert(@RequestBody TVAlertDto tvAlertDto);

  class FeignConfiguration {
    @Bean
    Logger.Level feignLoggerLevel() {
      return Logger.Level.FULL;
    }

    @Bean
    public ErrorDecoder feignErrorDecoder() {
      return AnnotationErrorDecoder
          .builderFor(RoutingClient.class)
          .build();
    }
  }
}
