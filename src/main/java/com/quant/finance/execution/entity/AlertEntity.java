package com.quant.finance.execution.entity;

import static jakarta.persistence.EnumType.STRING;

import com.ib.client.Types.Action;
import com.quant.finance.execution.enums.AlertState;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "alert")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class AlertEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(nullable = false, length = 32)
  private String symbol;
  @Enumerated(STRING)
  @Column(nullable = false, length = 20)
  private Action action;
  @Transient //todo add as column
  private String peerSymbol;
  @Transient
  private boolean isPeer;
  @Column(nullable = false, length = 200)
  private String strategy;
  @Column(length = 20)
  private String assetClass;
  @Column(length = 50)
  private String exchange;
  private Integer interval;
  private Integer volume;
  private String quote;
  @Column(precision = 15, scale = 2)
  private BigDecimal open;
  @Column(precision = 15, scale = 2)
  private BigDecimal close;
  @Column(precision = 15, scale = 2)
  private BigDecimal low;
  @Column(precision = 15, scale = 2)
  private BigDecimal high;
  private LocalDateTime barTime;
  private LocalDateTime generatedTime;
  @CreationTimestamp
  @Column(nullable = false)
  private LocalDateTime createdAt;
  @UpdateTimestamp
  @Column(nullable = false)
  private LocalDateTime updatedAt;
  @Column(nullable = false, length = 2000)
  private String json;
  @Enumerated(STRING)
  @Column(nullable = false, length = 20, columnDefinition = "RECEIVED")
  private AlertState state;
  @Column(length = 2000)
  private String description;

  public AlertEntity duplicateForPeerTickerProcessing() {
    return AlertEntity.builder()
        .id(this.id)
        .symbol(this.symbol)
        .action(this.action == Action.BUY ? Action.SELL : Action.BUY)
        .peerSymbol(this.peerSymbol)
        .isPeer(true)
        .assetClass(this.assetClass)
        .strategy(this.strategy)
        .exchange(this.exchange)
        .interval(this.interval)
        .volume(this.volume)
        .open(this.open)
        .close(this.close)
        .low(this.low)
        .high(this.high)
        .barTime(this.barTime)
        .generatedTime(this.generatedTime)
        .createdAt(this.createdAt)
        .updatedAt(this.updatedAt)
        .json(this.json)
        .state(this.state)
        .description(this.description)
        .build();
  }
}
