package com.quant.finance.execution.entity;

import static jakarta.persistence.EnumType.STRING;

import com.ib.client.OrderStatus;
import com.ib.client.OrderType;
import com.quant.finance.execution.enums.OrderAction;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
  private Long id;
  @Column(nullable = false, length = 255)
  private String brokerOrderId;
  @Column(length = 255)
  private String executionId;
  @Column(nullable = false)
  private Long alertId;
  @Column(nullable = false)
  private Integer strategyId;
  @Column(nullable = false, length = 32)
  private String symbol;
  private Integer contractId;
  @Enumerated(STRING)
  @Column(nullable = false, length = 20)
  private OrderAction action;
  @Column(nullable = false)
  private Double quantity;
  @Column(nullable = false, length = 3)
  private String currency;
  @Enumerated(STRING)
  @Column(nullable = false, length = 20)
  private OrderStatus status;
  @Enumerated(STRING)
  @Column(nullable = false, length = 20)
  private OrderType orderType;
  private Long parentOrderId;
  @Column(precision = 15, scale = 2)
  private BigDecimal limitPrice;
  @Column(precision = 15, scale = 2)
  private BigDecimal stopPrice;
  private Double filledQuantity;
  @Column(precision = 15, scale = 2)
  private BigDecimal averageFillPrice;
  @Column(precision = 15, scale = 2)
  private BigDecimal commission;
  private String errorMessage;
  private LocalDateTime submittedAt;
  private LocalDateTime filledAt;
  @CreationTimestamp
  @Column(nullable = false)
  private LocalDateTime createdAt;
  @UpdateTimestamp
  @Column(nullable = false)
  private LocalDateTime updatedAt;

}
