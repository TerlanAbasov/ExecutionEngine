package com.quant.finance.execution.error;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class IBErrorHandler {

  public void error(int id, long l, int code, String message, String s1) {
    log.error("ERROR. id: {}, code: {}, msg: {}", id, code, message);

    //if (i1 == 1100 || i1 == 2110) {
    //  log.warn("Connectivity lost (code {}), scheduling reconnect...", i1);
    //  scheduleReconnect();
    //}
  }
}
