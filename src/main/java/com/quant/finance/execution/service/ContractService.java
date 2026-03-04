package com.quant.finance.execution.service;

import com.ib.client.Contract;
import com.ib.client.ContractDetails;
import com.ib.client.Types;
import com.quant.finance.execution.client.IBClient;
import com.quant.finance.execution.util.EngineUtil;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContractService {
  @Lazy
  @Autowired
  private IBClient ibClient;
  private final NotificationService notificationService;

  private final Map<Integer, CompletableFuture<ContractDetails>> contractMap =
      new ConcurrentHashMap<>();

  public CompletableFuture<ContractDetails> requestContract(String symbol) {

    int requestId = EngineUtil.nextRequestId();

    CompletableFuture<ContractDetails> contractFuture = new CompletableFuture<>();
    contractMap.put(requestId, contractFuture);

    Contract contract = new Contract();
    contract.symbol(symbol);
    contract.secType(Types.SecType.STK);
    contract.exchange("SMART");

    ibClient.getEClientSocket().reqContractDetails(requestId, contract);

    return contractFuture;
  }

  public void onContractDetails(int requestId, ContractDetails contractDetails) {
    try {
      log.info("CONTRTACT DETAILS. RequestId: {},ContractDetails: {}", requestId,
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
    contractMap.remove(requestId);
  }
}
