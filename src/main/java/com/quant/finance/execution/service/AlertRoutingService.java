package com.quant.finance.execution.service;

import com.quant.finance.execution.client.RoutingClient;
import com.quant.finance.execution.dto.TVAlertDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertRoutingService {
  private final RoutingClient routingClient;

  @Async
  public void routeToPartner(TVAlertDto tvAlertDto) {
    try {
      if (tvAlertDto.getRouting() != null && tvAlertDto.getRouting().equals("true")) {
        routingClient.routeAlert(tvAlertDto);
      }
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }
}
