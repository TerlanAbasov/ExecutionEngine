package com.quant.finance.execution.service;

import static com.ib.client.Util.DoubleMaxString;
import static com.ib.client.Util.decimalToStringNoZero;

import com.ib.client.Contract;
import com.ib.client.Decimal;
import com.quant.finance.execution.client.IBClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {
  private final NotificationService notificationService;

  public void updateAccountValue(String key, String value, String currency, String accountName) {
    try {
      if (!IBClient.neededAccountSummaryTagList().contains(key)) {
        return;
      }

      String message = String.format("UpdateAccountValue. key: %s, value: %s, vurrency: %s," +
          " accountName: %s", key, value, currency, accountName);
      log.info(message);
      notificationService.notify(message);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }

  public void updatePortfolio(Contract contract, Decimal position, double marketPrice,
                              double marketValue, double averageCost, double unrealizedPNL,
                              double realizedPNL, String accountName) {
    try {
      String message = String.format("UpdatePortfolio. symbol: %s, secType: %s, exchange: %s," +
              " position: %s, marketPrice: %s, marketValue: %s, averageCost: %s, unrealizedPNL: %s," +
              " realizedPNL: %s, accountName: %s",
          contract.symbol(), contract.secType().name(), contract.exchange(),
          decimalToStringNoZero(position), DoubleMaxString(marketPrice),
          DoubleMaxString(marketValue), DoubleMaxString(averageCost),
          DoubleMaxString(unrealizedPNL), DoubleMaxString(realizedPNL), accountName);
      log.info(message);
      notificationService.notify(message);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }

  public void updateAccountTime(String timestamp) {
    try {
      String message = String.format("UpdateAccountTime. time: %s", timestamp);
      log.info(message);
      notificationService.notify(message);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }

  public void accountDownloadEnd(String account) {
    try {
      String message = String.format("Download finished for account: %s", account);
      log.info(message);
      notificationService.notify(message);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }
}
