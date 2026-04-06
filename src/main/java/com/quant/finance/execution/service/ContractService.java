package com.quant.finance.execution.service;

import com.ib.client.Contract;
import com.ib.client.ContractDetails;
import com.ib.client.Types;
import com.quant.finance.execution.client.IBClient;
import com.quant.finance.execution.config.ApplicationProperties;
import com.quant.finance.execution.entity.AlertEntity;
import com.quant.finance.execution.util.EngineUtil;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ContractService {
  private final IBClient ibClient;
  private final NotificationService notificationService;
  private final ApplicationProperties properties;

  private final Map<Integer, CompletableFuture<ContractDetails>> contractMap =
      new ConcurrentHashMap<>();

  public ContractService(@Lazy IBClient ibClient, NotificationService notificationService,
                         ApplicationProperties properties) {
    this.ibClient = ibClient;
    this.notificationService = notificationService;
    this.properties = properties;
  }

  public CompletableFuture<ContractDetails> requestContract(AlertEntity alert) {

    int requestId = EngineUtil.nextRequestId();

    CompletableFuture<ContractDetails> contractFuture = new CompletableFuture<>();
    contractMap.put(requestId, contractFuture);

    Contract contract = buildContract(alert);

    log.error(contract.toString());
    ibClient.getEClientSocket().reqContractDetails(requestId, contract);

    return contractFuture.orTimeout(properties.getParams().getContractFutureTimeout(),
        TimeUnit.SECONDS);
  }

  public void onContractDetails(int requestId, ContractDetails contractDetails) {
    try {
      log.info("CONTRTACT DETAILS. RequestId={}, ContractDetails={}", requestId,
          contractDetails.toString());

      CompletableFuture<ContractDetails> future = contractMap.get(requestId);

      if (future != null && !future.isDone()) {
        future.complete(contractDetails);
      }
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      notificationService.notify(e.getMessage());
    }
  }

  public void onContractDetailsEnd(int requestId) {
    log.info("CONTRTACT DETAILS END. id={}", requestId);
    contractMap.remove(requestId);
  }

  private Contract buildContract(AlertEntity alert) {
    Contract contract = new Contract();

    String symbol = alert.getSymbol().toUpperCase();
    Types.SecType secType = defineSecType(alert.getAssetClass());

    contract.secType(secType);

    if (Objects.requireNonNull(secType) == Types.SecType.CRYPTO) {
      String base = EngineUtil.extractCryptoBaseSymbol(symbol);
      String quote = EngineUtil.extractCryptoQuote(symbol);

      contract.symbol(base);
      contract.exchange("PAXOS"); // REQUIRED for crypto
      contract.currency(quote);
    } else {
      contract.symbol(symbol);
      contract.exchange("SMART");
      contract.currency("USD");
    }

    return contract;
  }

  private Types.SecType defineSecType(String assetClass) {
    if (assetClass == null) {
      return Types.SecType.STK;
    }

    return assetClass.equals("CRYPTO") ? Types.SecType.CRYPTO : Types.SecType.STK;
  }
}
