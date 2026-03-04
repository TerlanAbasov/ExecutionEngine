package com.quant.finance.execution.service;

import com.ib.client.Decimal;
import com.ib.client.Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PnlService {
  private final NotificationService notificationService;

  public void pnl(int requestId, double dailyPnL, double unrealizedPnl, double realizedPnl) {
    String message =
        String.format("PNL. RequestId: %d, dailyPnl: %.2f, unrealizedPnl: %.2f, realizedPnl: %.2f",
            requestId, dailyPnL, unrealizedPnl, realizedPnl);

    log.info(message);
    notificationService.notify(message);
  }

  public void pnlSingle(int requestId, Decimal pos, double dailyPnL, double unrealizedPnl,
                        double realizedPnl, double value) {
    String message = String.format(
        "PNL SINGLE. RequestId: %d, pos: %s, dailyPnl: %.2f, unrealizedPnl: %.2f, realizedPnL: %s, value: %.2f",
        requestId, pos.toString(), Util.DoubleMaxString(dailyPnL),
        Util.DoubleMaxString(unrealizedPnl),
        Util.DoubleMaxString(realizedPnl), value);

    log.info(message);
    notificationService.notify(message);
  }
}
