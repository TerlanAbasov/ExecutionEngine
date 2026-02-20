package com.quant.finance.execution.service;

import com.ib.client.Contract;
import com.ib.client.Decimal;
import com.ib.client.Order;
import com.quant.finance.execution.client.IBClient;
import com.quant.finance.execution.entity.AlertEntity;
import com.quant.finance.execution.entity.OrderEntity;
import com.quant.finance.execution.entity.StrategyEntity;
import com.quant.finance.execution.repository.OrderRepository;
import com.quant.finance.execution.util.EngineUtil;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradeService {
  private final OrderRepository repository;
  private final IBClient ibClient;
  private final OrderService orderService;
  private final StrategyService strategyService;
  private final EWrapperImpl eWrapper;

  @Transactional
  public void placeOrder(AlertEntity alert, StrategyEntity strategy, Contract contract) {
    OrderEntity orderEntity = orderService.createOrderEntity(alert, strategy, contract);

    Order tradeOrder = new Order();
    tradeOrder.action(orderEntity.getAction().name());
    tradeOrder.orderType(orderEntity.getOrderType().name());
    tradeOrder.totalQuantity(Decimal.get(orderEntity.getQuantity()));
    tradeOrder.orderId(Integer.parseInt(orderEntity.getBrokerOrderId()));
    //todo order with strategy amount not quantity

    //todo use existing contract
    Contract tradeContract = createContract(orderEntity);
    tradeContract.symbol(contract.symbol());

    tradeContract.conid(contract.conid());
    tradeContract.exchange(contract.exchange());

    orderService.save(orderEntity);
    //todo is this line needed?
    ibClient.getContractDetails(EngineUtil.nextRequestId(), contract);
    ibClient.placeOrder(tradeContract, tradeOrder);

    //todo LS and TP orders
  }

  private Contract createContract(OrderEntity orderEntity) {
    Contract contract = new Contract();
    contract.secType("STK");
    contract.currency(orderEntity.getCurrency());
    contract.exchange("SMART");
    contract.symbol(orderEntity.getSymbol());

    return contract;
  }

  private BigDecimal calculateLimitPrice() {
    return null;
  }

  private BigDecimal calculateStopPrice() {
    return null;
  }

  public void orderBuy() {
    // todo
  }

  public void orderTakeProfit() {
    // todo
  }

  public void orderStopLoss() {
    // todo
  }

  /*
  import com.ib.client.*;

public class BracketOrderExample {

    private final EClientSocket client;
    private int nextOrderId;

    public BracketOrderExample(EClientSocket client, int nextOrderId) {
        this.client = client;
        this.nextOrderId = nextOrderId;
    }

    public void placeBuyWithTakeProfitAndStopLoss() {

        Contract contract = createContract();

        int parentId = nextOrderId;

        double quantity = 100;
        double takeProfitPrice = 110.0;
        double stopLossPrice = 95.0;

        // =====================
        // PARENT BUY ORDER
        // =====================
        Order parent = new Order();
        parent.orderId(parentId);
        parent.action("BUY");
        parent.orderType("MKT");          // Or "LMT"
        parent.totalQuantity(quantity);
        parent.transmit(false);

        // =====================
        // TAKE PROFIT
        // =====================
        Order takeProfit = new Order();
        takeProfit.orderId(parentId + 1);
        takeProfit.action("SELL");
        takeProfit.orderType("LMT");
        takeProfit.lmtPrice(takeProfitPrice);
        takeProfit.totalQuantity(quantity);
        takeProfit.parentId(parentId);
        takeProfit.transmit(false);

        // =====================
        // STOP LOSS
        // =====================
        Order stopLoss = new Order();
        stopLoss.orderId(parentId + 2);
        stopLoss.action("SELL");
        stopLoss.orderType("STP");
        stopLoss.auxPrice(stopLossPrice);
        stopLoss.totalQuantity(quantity);
        stopLoss.parentId(parentId);
        stopLoss.transmit(true);   // LAST ORDER TRANSMITS ALL

        // =====================
        // PLACE ORDERS
        // =====================
        client.placeOrder(parent.orderId(), contract, parent);
        client.placeOrder(takeProfit.orderId(), contract, takeProfit);
        client.placeOrder(stopLoss.orderId(), contract, stopLoss);

        nextOrderId += 3;
    }

    private Contract createContract() {
        Contract contract = new Contract();
        contract.symbol("AAPL");
        contract.secType("STK");
        contract.exchange("SMART");
        contract.currency("USD");
        contract.primaryExch("NASDAQ");
        return contract;
    }
}

   */
}
