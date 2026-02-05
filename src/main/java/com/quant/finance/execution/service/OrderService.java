package com.quant.finance.execution.service;

import com.ib.client.EClient;
import com.ib.client.EClientSocket;
import com.ib.client.EJavaSignal;
import com.quant.finance.execution.client.IBClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrderService {
  private final IBClient ibClient = IBClient.getInstance();

  public void requestOpenOrders(){
    ibClient.getOpenOrders();
  }
}
