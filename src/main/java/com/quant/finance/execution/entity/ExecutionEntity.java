package com.quant.finance.execution.entity;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//@Entity
//@Table(name = "order_detail")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExecutionEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  //@OneToOne(fetch = FetchType.LAZY)
  //@JoinColumn(name = "order_id", nullable = false, unique = true,
  //    foreignKey = @ForeignKey(name = "fk_order_detail_order"))
  private OrderEntity order;
  @Column(length = 255)
  private String execId;
  @Column(nullable = false)
  private Long alertId;
  @Column(nullable = false)
  private Integer strategyId;
  private Integer contractId;
  @Column(precision = 15, scale = 2)
  private BigDecimal price;
  @Column(nullable = false)
  private Double quantity;
  @Column(precision = 15, scale = 2)
  private BigDecimal commission;
  @Column(precision = 15, scale = 2)
  private BigDecimal totalAmount;
  @Column(nullable = false, length = 3)
  private String currency;
  @Column(precision = 15, scale = 2)
  private BigDecimal limitPrice;
  @Column(precision = 15, scale = 2)
  private BigDecimal stopPrice;
  private LocalDateTime submittedAt;
  private LocalDateTime filledAt;
  @Column(nullable = false)
  private String errorMessage;
}
