package com.quant.finance.execution.model;

import com.ib.client.Contract;
import com.ib.client.Decimal;
import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ContractData {
  private String symbol;
  private Integer contractId;
  private String securityType;
  private String currency;
  @Builder.Default
  private Double quantity = 0d;
  private Double averageCost;
  private Double dailyPnL;
  private Double unrealizedPnl;
  private Double realizedPnl;
  private Double value;

  //todo consider should equals and hashcode be overriten.

  public static ContractData buildContractData(Contract contract, Decimal quantity,
                                               double avgCost) {
    ContractData contractData = ContractData.builder()
        .symbol(contract.symbol())
        .securityType(contract.getSecType())
        .contractId(contract.conid())
        .currency(contract.currency())
        .averageCost(scaleDoubleValue(avgCost))
        .quantity(quantity.value().doubleValue())
        .build();

    return contractData;
  }

  public static Double scaleDoubleValue(String value) {
    if (value == null || value.isEmpty()) {
      return 0d;
    }

    BigDecimal bigDecimal = new BigDecimal(value);
    return bigDecimal
        .setScale(2, RoundingMode.HALF_DOWN).doubleValue();
  }

  public static Double scaleDoubleValue(double value) {
    return BigDecimal.valueOf(value)
        .setScale(2, RoundingMode.HALF_DOWN).doubleValue();
  }
}
