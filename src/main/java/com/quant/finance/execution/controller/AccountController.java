package com.quant.finance.execution.controller;

import com.quant.finance.execution.dto.AccountCommandDto;
import com.quant.finance.execution.service.AccountCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/account")
@RequiredArgsConstructor
public class AccountController {
  private final AccountCommandService commandService;

  @PostMapping("/command")
  public ResponseEntity<Void> onUpdate(@RequestBody AccountCommandDto commandDto) {
    commandService.executeCommand(commandDto);
    return ResponseEntity.ok().build();
  }
}
