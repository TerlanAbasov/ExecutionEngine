package com.quant.finance.execution.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ib.client.Contract;
import com.ib.client.Decimal;
import com.ib.client.Order;
import com.quant.finance.execution.client.DiscordClient;
import com.quant.finance.execution.client.IBClient;
import com.quant.finance.execution.client.TelegramClient;
import com.quant.finance.execution.dto.TVAlert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExecutionService {

  private final DiscordClient discordClient;
  private final TelegramClient telegramClient;
  private final IBClient ibClient = IBClient.getInstance();
  private final ObjectMapper objectMapper = new ObjectMapper();

  public void executeStrategy(TVAlert alert) {
    //DiscordMessage message = new DiscordMessage("Execution Engine", alert.toString());
    //discordClient.notify(message);

    //ibClient.connect();

    try {
      telegramClient.sendMessage("8068983143:AAGyxjjqig8ZJAjBFuxdg8Obwy-Y41OKCdA", "1014578999",
          convertToJson(alert));
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }

    try {
      Thread.sleep(0); // wait for nextValidId
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    // ===== Contract =====
    Contract contract = new Contract();
    contract.symbol("AAPL");
    contract.secType("STK");
    contract.currency("USD");
    contract.exchange("SMART");
    contract.primaryExch("NASDAQ");

    // ===== Order =====
    Order order = new Order();
    order.action("BUY");
    order.orderType("MKT");
    order.totalQuantity(Decimal.get(1L));

    //ibClient.placeOrder(contract, order);
    //ibClient.getOpenOrders();
  }

  public String convertToJson(TVAlert model) throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.writerWithDefaultPrettyPrinter()
        .writeValueAsString(model);
  }
}
