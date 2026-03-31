package com.quant.finance.execution.config;

import com.quant.finance.execution.interceptor.StartupDelayInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

  private final ApplicationProperties properties;

  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new StartupDelayInterceptor(properties));
  }
}