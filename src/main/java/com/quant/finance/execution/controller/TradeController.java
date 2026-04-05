package com.quant.finance.execution.controller;

import com.quant.finance.execution.dto.TradeCommandDto;
import com.quant.finance.execution.command.CommandDispatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/trades")
@RequiredArgsConstructor
public class  TradeController {

  private final CommandDispatcher commandDispatcher;

  @PostMapping("/command")
  public ResponseEntity<String> executeCommand(@RequestBody TradeCommandDto commandDto) {
    return ResponseEntity.ok(commandDispatcher.dispatch(commandDto));
  }
}
