package com.quant.finance.execution.model;

import com.ib.client.Contract;
import com.ib.client.Decimal;
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
  private String dailyPnL;
  private String unrealizedPnl;
  private String realizedPnl;
  private Double value;

  //todo consider should equals and hashcode be overriten.


  public static ContractData buildContractData(Contract contract, Decimal quantity,
                                                double avgCost) {
    ContractData contractData = ContractData.builder()
        .symbol(contract.symbol())
        .securityType(contract.getSecType())
        .contractId(contract.conid())
        .currency(contract.currency())
        .averageCost(avgCost)
        .quantity(quantity.value().doubleValue())
        .build();

    return contractData;
  }
}
