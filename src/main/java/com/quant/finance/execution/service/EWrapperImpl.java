package com.quant.finance.execution.service;

import com.ib.client.Bar;
import com.ib.client.CommissionAndFeesReport;
import com.ib.client.Contract;
import com.ib.client.ContractDescription;
import com.ib.client.ContractDetails;
import com.ib.client.Decimal;
import com.ib.client.DeltaNeutralContract;
import com.ib.client.DepthMktDataDescription;
import com.ib.client.EWrapper;
import com.ib.client.Execution;
import com.ib.client.FamilyCode;
import com.ib.client.HistogramEntry;
import com.ib.client.HistoricalSession;
import com.ib.client.HistoricalTick;
import com.ib.client.HistoricalTickBidAsk;
import com.ib.client.HistoricalTickLast;
import com.ib.client.NewsProvider;
import com.ib.client.Order;
import com.ib.client.OrderState;
import com.ib.client.PriceIncrement;
import com.ib.client.SoftDollarTier;
import com.ib.client.TickAttrib;
import com.ib.client.TickAttribBidAsk;
import com.ib.client.TickAttribLast;
import com.ib.client.protobuf.ErrorMessageProto;
import com.ib.client.protobuf.ExecutionDetailsEndProto;
import com.ib.client.protobuf.ExecutionDetailsProto;
import com.ib.client.protobuf.OpenOrderProto;
import com.ib.client.protobuf.OpenOrdersEndProto;
import com.ib.client.protobuf.OrderStatusProto;
import com.quant.finance.execution.client.IBClient;
import com.quant.finance.execution.config.ApplicationProperties;
import com.quant.finance.execution.error.IBErrorHandler;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EWrapperImpl implements EWrapper {

  //private static EClientSocket eClientSocket = null;
  private final NotificationService notificationService;
  private final ApplicationProperties properties;
  private final PositionService positionService;
  private final ContractService contractService;
  private final OrderService orderService;
  private final PnlService pnlService;
  private final ExecutionService executionService;
  private final IBErrorHandler ibErrorHandler;

  //@PostConstruct
  //public void connect() {
  //  EJavaSignal signal = new EJavaSignal();
  //  eClientSocket = new EClientSocket(this, signal);
  //
  //  log.info("Connecting to {}:{}", properties.getClient().getGateway().getHost(),
  //      properties.getClient().getGateway().getPort());
  //
  //  eClientSocket.eConnect(properties.getClient().getGateway().getHost(),
  //      properties.getClient().getGateway().getPort(), properties.getClient().getGateway().getId());
  //
  //  EReader reader = new EReader(eClientSocket, signal);
  //  reader.start();
  //
  //  log.info("Connected to {}:{}", properties.getClient().getGateway().getHost(),
  //      properties.getClient().getGateway().getPort());
  //
  //  new Thread(() -> {
  //    while (eClientSocket.isConnected()) {
  //      signal.waitForSignal();
  //      try {
  //        reader.processMsgs();
  //      } catch (Exception e) {
  //        log.error(e.getMessage(), e);
  //      }
  //    }
  //  }).start();
  //}
  //
  //public void disconnect() {
  //  eClientSocket.eDisconnect();
  //}
  //
  //public EClientSocket getEClientSocket() {
  //  if (!eClientSocket.isConnected()) {
  //    disconnect();
  //    connect();
  //  }
  //
  //  return eClientSocket;
  //}

  @Override
  public void tickPrice(int i, int i1, double v, TickAttrib tickAttrib) {
  }

  @Override
  public void tickSize(int i, int i1, Decimal decimal) {
  }

  @Override
  public void tickOptionComputation(int i, int i1, int i2, double v, double v1, double v2,
                                    double v3, double v4, double v5, double v6, double v7) {
  }

  @Override
  public void tickGeneric(int i, int i1, double v) {
  }

  @Override
  public void tickString(int i, int i1, String s) {
  }

  @Override
  public void tickEFP(int i, int i1, double v, String s, double v1, int i2, String s1, double v2,
                      double v3) {
  }

  @Override
  public void orderStatus(int orderId, String status, Decimal filled, Decimal remaining,
                          double avgFillPrice, long l, int i1, double v1, int i2, String s1,
                          double v2) {
    orderService.orderStatus(orderId, status, filled, remaining, avgFillPrice, l, i1, v1, i2, s1,
        v2);
  }

  @Override
  public void openOrder(int i, Contract contract, Order order, OrderState orderState) {
    log.info("OPEN ORDER. orderId: {}, status: {}", i, orderState.status());
  }

  @Override
  public void openOrderEnd() {
    log.info("OPEN ORDER END");
  }

  @Override
  public void updateAccountValue(String s, String s1, String s2, String s3) {
  }

  @Override
  public void updatePortfolio(Contract contract, Decimal decimal, double v, double v1, double v2,
                              double v3, double v4, String s) {
  }

  @Override
  public void updateAccountTime(String s) {
  }

  @Override
  public void accountDownloadEnd(String s) {
  }

  @Override
  public void nextValidId(int id) {
    IBClient.setNextOrderId(id);
    log.info("\uD83D\uDE80 Next valid orderId: {}", id);
  }

  @Override
  public void contractDetails(int requestId, ContractDetails contractDetails) {
    contractService.onContractDetails(requestId, contractDetails);
  }

  @Override
  public void bondContractDetails(int i, ContractDetails contractDetails) {

  }

  @Override
  public void contractDetailsEnd(int i) {
    log.info("CONTRTACT DETAILS END. id: {}", i);

  }

  @Override
  public void execDetails(int id, Contract contract, Execution execution) {
    executionService.execDetails(id, contract, execution);
  }

  @Override
  public void execDetailsEnd(int i) {
  }

  @Override
  public void updateMktDepth(int i, int i1, int i2, int i3, double v, Decimal decimal) {
  }

  @Override
  public void updateMktDepthL2(int i, int i1, String s, int i2, int i3, double v, Decimal decimal,
                               boolean b) {
  }

  @Override
  public void updateNewsBulletin(int i, int i1, String s, String s1) {
  }

  @Override
  public void managedAccounts(String s) {
  }

  @Override
  public void receiveFA(int i, String s) {
  }

  @Override
  public void historicalData(int i, Bar bar) {
  }

  @Override
  public void scannerParameters(String s) {

  }

  @Override
  public void scannerData(int i, int i1, ContractDetails contractDetails, String s, String s1,
                          String s2, String s3) {

  }

  @Override
  public void scannerDataEnd(int i) {

  }

  @Override
  public void realtimeBar(int i, long l, double v, double v1, double v2, double v3, Decimal decimal,
                          Decimal decimal1, int i1) {

  }

  @Override
  public void currentTime(long l) {

  }

  @Override
  public void fundamentalData(int i, String s) {

  }

  @Override
  public void deltaNeutralValidation(int i, DeltaNeutralContract deltaNeutralContract) {

  }

  @Override
  public void tickSnapshotEnd(int i) {

  }

  @Override
  public void marketDataType(int i, int i1) {

  }

  @Override
  public void commissionAndFeesReport(CommissionAndFeesReport report) {
    executionService.commissionAndFeesReport(report);
  }

  @Override
  public void position(String account, Contract contract, Decimal quantity, double avgCost) {
    positionService.onPosition(account, contract, quantity, avgCost);
  }

  @Override
  public void positionEnd() {
    positionService.onPositionEnd();
  }

  @Override
  public void accountSummary(int i, String s, String s1, String s2, String s3) {
  }

  @Override
  public void accountSummaryEnd(int i) {
  }

  @Override
  public void verifyMessageAPI(String s) {
  }

  @Override
  public void verifyCompleted(boolean b, String s) {
  }

  @Override
  public void verifyAndAuthMessageAPI(String s, String s1) {
  }

  @Override
  public void verifyAndAuthCompleted(boolean b, String s) {
  }

  @Override
  public void displayGroupList(int i, String s) {
  }

  @Override
  public void displayGroupUpdated(int i, String s) {
  }

  @Override
  public void error(Exception e) {
  }

  @Override
  public void error(String s) {
  }

  @Override
  public void error(int i, long l, int i1, String s, String s1) {
    ibErrorHandler.handleError(i, l, i1, s, s1);

    //todo

    /*
    * if (errorCode == 502 || errorCode == 504) {

        log.warn("Lost TWS connection. Reconnecting...");

        client.eDisconnect();

        new Thread(() -> {
            while (!client.isConnected()) {
                try {
                    Thread.sleep(3000);
                    client.eConnect(host, port, clientId);
                } catch (Exception e) {
                    log.error("Reconnect failed", e);
                }
            }
        }).start();
    }
    * */
  }

  @Override
  public void connectionClosed() {
  }

  @Override
  public void connectAck() {
    //if (eClientSocket.isAsyncEConnect()) {
    //  eClientSocket.startAPI();
    //}
    IBClient.startAPI();
  }

  @Override
  public void positionMulti(int i, String s, String s1, Contract contract, Decimal decimal,
                            double v) {
  }

  @Override
  public void positionMultiEnd(int i) {
  }

  @Override
  public void accountUpdateMulti(int i, String s, String s1, String s2, String s3, String s4) {
  }

  @Override
  public void accountUpdateMultiEnd(int i) {
  }

  @Override
  public void securityDefinitionOptionalParameter(int i, String s, int i1, String s1, String s2,
                                                  Set<String> set, Set<Double> set1) {
  }

  @Override
  public void securityDefinitionOptionalParameterEnd(int i) {
  }

  @Override
  public void softDollarTiers(int i, SoftDollarTier[] softDollarTiers) {
  }

  @Override
  public void familyCodes(FamilyCode[] familyCodes) {

  }

  @Override
  public void symbolSamples(int i, ContractDescription[] contractDescriptions) {

  }

  @Override
  public void historicalDataEnd(int i, String s, String s1) {

  }

  @Override
  public void mktDepthExchanges(DepthMktDataDescription[] depthMktDataDescriptions) {

  }

  @Override
  public void tickNews(int i, long l, String s, String s1, String s2, String s3) {

  }

  @Override
  public void smartComponents(int i, Map<Integer, Map.Entry<String, Character>> map) {

  }

  @Override
  public void tickReqParams(int i, double v, String s, int i1) {
  }

  @Override
  public void newsProviders(NewsProvider[] newsProviders) {
  }

  @Override
  public void newsArticle(int i, int i1, String s) {
  }

  @Override
  public void historicalNews(int i, String s, String s1, String s2, String s3) {
  }

  @Override
  public void historicalNewsEnd(int i, boolean b) {
  }

  @Override
  public void headTimestamp(int i, String s) {
  }

  @Override
  public void histogramData(int i, List<HistogramEntry> list) {
  }

  @Override
  public void historicalDataUpdate(int i, Bar bar) {

  }

  @Override
  public void rerouteMktDataReq(int i, int i1, String s) {
  }

  @Override
  public void rerouteMktDepthReq(int i, int i1, String s) {
  }

  @Override
  public void marketRule(int i, PriceIncrement[] priceIncrements) {
  }

  @Override
  public void pnl(int requestId, double dailyPnL, double unrealizedPnl, double realizedPnl) {
    pnlService.pnl(requestId, dailyPnL, unrealizedPnl, realizedPnl);
  }

  @Override
  public void pnlSingle(int requestId, Decimal pos, double dailyPnL, double unrealizedPnl,
                        double realizedPnl, double value) {
    pnlService.pnlSingle(requestId, pos, dailyPnL, unrealizedPnl, realizedPnl, value);
  }

  @Override
  public void historicalTicks(int i, List<HistoricalTick> list, boolean b) {
  }

  @Override
  public void historicalTicksBidAsk(int i, List<HistoricalTickBidAsk> list, boolean b) {
  }

  @Override
  public void historicalTicksLast(int i, List<HistoricalTickLast> list, boolean b) {
  }

  @Override
  public void tickByTickAllLast(int i, int i1, long l, double v, Decimal decimal,
                                TickAttribLast tickAttribLast, String s, String s1) {
  }

  @Override
  public void tickByTickBidAsk(int i, long l, double v, double v1, Decimal decimal,
                               Decimal decimal1, TickAttribBidAsk tickAttribBidAsk) {
  }

  @Override
  public void tickByTickMidPoint(int i, long l, double v) {
  }

  @Override
  public void orderBound(long l, int i, int i1) {
  }

  @Override
  public void completedOrder(Contract contract, Order order, OrderState orderState) {
  }

  @Override
  public void completedOrdersEnd() {
  }

  @Override
  public void replaceFAEnd(int i, String s) {
  }

  @Override
  public void wshMetaData(int i, String s) {
  }

  @Override
  public void wshEventData(int i, String s) {
  }

  @Override
  public void historicalSchedule(int i, String s, String s1, String s2,
                                 List<HistoricalSession> list) {
  }

  @Override
  public void userInfo(int i, String s) {
  }

  @Override
  public void currentTimeInMillis(long l) {
  }

  @Override
  public void orderStatusProtoBuf(OrderStatusProto.OrderStatus orderStatus) {
  }

  @Override
  public void openOrderProtoBuf(OpenOrderProto.OpenOrder openOrder) {
  }

  @Override
  public void openOrdersEndProtoBuf(OpenOrdersEndProto.OpenOrdersEnd openOrdersEnd) {
  }

  @Override
  public void errorProtoBuf(ErrorMessageProto.ErrorMessage errorMessage) {
  }

  @Override
  public void execDetailsProtoBuf(ExecutionDetailsProto.ExecutionDetails executionDetails) {
  }

  @Override
  public void execDetailsEndProtoBuf(
      ExecutionDetailsEndProto.ExecutionDetailsEnd executionDetailsEnd) {
  }
}
