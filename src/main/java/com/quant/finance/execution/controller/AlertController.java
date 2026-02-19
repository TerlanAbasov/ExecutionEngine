package com.quant.finance.execution.controller;

import com.quant.finance.execution.dto.TVAlertDto;
import com.quant.finance.execution.service.ExecutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
public class AlertController {

  private final ExecutionService executionService;

  @PostMapping("/tv-hook")
  public void alert(@RequestBody TVAlertDto tvAlertDto) {
    executionService.executeStrategy(tvAlertDto);
  }
}
