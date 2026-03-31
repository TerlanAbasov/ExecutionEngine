package com.quant.finance.execution.interceptor;

import com.quant.finance.execution.config.ApplicationProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class StartupDelayInterceptor implements HandlerInterceptor {

  private final ApplicationProperties properties;

  private final long startTime = System.currentTimeMillis();

  public StartupDelayInterceptor(ApplicationProperties properties) {
    this.properties = properties;
  }

  @Override
  public boolean preHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler) throws Exception {

    if (System.currentTimeMillis() - startTime < properties.getParams().getStartUpDelay()) {
      response.setStatus(503);
      response.getWriter().write("Warming up");
      return false;
    }

    return true;
  }
}
