package com.quant.finance.execution.service;

import com.quant.finance.execution.model.OrderResult;
import org.springframework.stereotype.Service;

@Service
public class NotificationFormatterService {

  public String formatOrderResult(OrderResult result) {
    String icon = result.isSuccess() ? "✅" : "❌";
    if (!result.isSuccess()) {
      return icon + " *Order Failed*\n" + result.getMessage();
    }

    StringBuilder sb = new StringBuilder();
    sb.append(icon).append(" *").append(result.getMessage()).append("*\n");
    if (result.getSymbol() != null) {
      sb.append("Symbol: `").append(result.getSymbol()).append("`\n");
    }
    if (result.getSide() != null) {
      sb.append("Side: `").append(result.getSide()).append("`\n");
    }
    if (result.getQuantity() != null) {
      sb.append("Qty: `").append(result.getQuantity()).append("`\n");
    }
    if (result.getPrice() != null) {
      sb.append("Price: `").append(result.getPrice()).append("`\n");
    }
    if (result.getOrderId() != null) {
      sb.append("OrderId: `").append(result.getOrderId()).append("`");
    }
    return sb.toString();
  }

  // Call this from your engine when you want to push a notification proactively
  public String formatEngineAlert(String type, String details) {
    return "🔔 *Engine Alert: " + type + "*\n" + details;
  }
}
