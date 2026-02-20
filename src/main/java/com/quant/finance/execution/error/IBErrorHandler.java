package com.quant.finance.execution.error;

import com.quant.finance.execution.service.EWrapperImpl;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class IBErrorHandler {

  private final EWrapperImpl eWrapperimpl;

  public IBErrorHandler(@Lazy EWrapperImpl eWrapperimpl) {
    this.eWrapperimpl = eWrapperimpl;
  }

  public void error(int id, long l, int code, String message, String s1) {
    if (List.of(2104, 2158, 2106, 2107, 2108, 1101, 1102).contains(code)) {
      log.info("ERROR. id: {}, code: {}, msg: {}", id, code, message);
    } else if (List.of(504, 1100, 2110).contains(code)) {
      log.error("CONNECTION ERROR. id: {}, code: {}, msg: {}", id, code, message);
      eWrapperimpl.makeConnection();
    } else {
      log.error("ERROR. id: {}, code: {}, msg: {}", id, code, message);
    }

    //if (i1 == 1100 || i1 == 2110) {
    //  log.warn("Connectivity lost (code {}), scheduling reconnect...", i1);
    //  scheduleReconnect();
    //}
  }
}
