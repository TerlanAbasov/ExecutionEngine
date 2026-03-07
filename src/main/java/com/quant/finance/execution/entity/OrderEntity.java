package com.quant.finance.execution.entity;

import static jakarta.persistence.EnumType.STRING;

import com.ib.client.OrderStatus;
import com.ib.client.OrderType;
import com.ib.client.Types;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "order")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;
  @Column(nullable = false)
  private Integer brokerOrderId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "strategy_id", nullable = false,
      foreignKey = @ForeignKey(name = "fk_order_strategy"))
  private StrategyEntity strategy;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "alert_id")
  private AlertEntity alert;

  @Column(nullable = false, length = 32)
  private String symbol;
  private Integer contractId;
  private Integer parentOrderId;
  @Enumerated(STRING)
  @Column(nullable = false, length = 20)
  private Types.Action action;
  @Enumerated(STRING)
  @Column(nullable = false, length = 20)
  private OrderStatus status;
  @Enumerated(STRING)
  @Column(nullable = false, length = 20)
  private OrderType orderType;
  @Column(nullable = false)
  private Double quantity;
  @Column(precision = 15, scale = 2)
  private BigDecimal limitPrice;
  @Column(precision = 15, scale = 2)
  private BigDecimal takeProfitPrice;
  @Column(precision = 15, scale = 2)
  private BigDecimal stopLossPrice;
  private LocalDateTime submittedAt;
  private LocalDateTime filledAt;
  @CreationTimestamp
  @Column(nullable = false)
  private LocalDateTime createdAt;
  @UpdateTimestamp
  @Column(nullable = false)
  private LocalDateTime updatedAt;
  private String errorMessage;

  @OneToMany(mappedBy = "order", cascade = {CascadeType.PERSIST, CascadeType.MERGE},
      fetch = FetchType.LAZY)
  @Setter(AccessLevel.NONE)
  @Builder.Default
  private List<ExecutionEntity> executions = new ArrayList<>();

  public void addExecution(ExecutionEntity execution) {
    if (execution == null) {
      return;
    }

    this.executions.add(execution);
    execution.setOrder(this);
  }
}
