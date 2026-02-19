package com.quant.finance.execution.service;

import com.quant.finance.execution.client.IBClient;
import com.quant.finance.execution.config.ApplicationProperties;
import com.quant.finance.execution.dto.AccountCommandDto;
import com.quant.finance.execution.util.EngineUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountCommandService {
  private final IBClient ibClient;
  private final EWrapperImpl eWrapper;
  private final ApplicationProperties properties;

  public void executeCommand(AccountCommandDto commandDto) {
    if (commandDto.getMessage().getCommand().startsWith("/BUY")) {
      // parse symbol
    }

    switch (commandDto.getMessage().getCommand()) {
      case "POSITIONS":
        eWrapper.getEClientSocket().reqPositions();
        break;
      case "PNL":
        eWrapper.getEClientSocket()
            .reqPnL(EngineUtil.nextRequestId(), properties.getAccount().getId(), "");
        // TODO: 10.02.26 store requestId for cancellation
        break;
      case "CANCEL_PNL":
        eWrapper.getEClientSocket()
            .cancelPnL(EngineUtil.nextRequestId());
        break;
      case "PNL_SINGLE":
        eWrapper.getEClientSocket()
            .reqPnLSingle(EngineUtil.nextRequestId(), properties.getAccount().getId(),
                "", 753278772); // TSLR 649964878
        eWrapper.getEClientSocket()
            .reqPnLSingle(EngineUtil.nextRequestId(), properties.getAccount().getId(),
                "", 649964878);
        break;
      case "CANCEL_PNL_SINGLE":
        eWrapper.getEClientSocket()
            .cancelPnLSingle(EngineUtil.nextRequestId());
        break;
      case "ORDERS":
        eWrapper.getEClientSocket().reqOpenOrders();
        break;
      case "COMISSION":
        // TODO: 10.02.26
        break;
      default:
        log.warn("Unknown Command: {}", commandDto.getMessage().getCommand());
    }
  }

}
