package com.quant.finance.execution.controller;

import com.quant.finance.execution.dto.OrderCancelDto;
import com.quant.finance.execution.service.TradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

  private final TradeService tradeService;

  @PostMapping("/cancel")
  public ResponseEntity<Void> onUpdate(@RequestBody OrderCancelDto dto) {
    tradeService.cancelOrder(dto);
    return ResponseEntity.ok().build();
  }
}
