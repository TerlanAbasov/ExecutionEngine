package com.quant.finance.execution.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "execution")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExecutionEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "order_id", nullable = false,
      foreignKey = @ForeignKey(name = "fk_execution_order"))
  private OrderEntity order;
  @Column(unique = true, length = 255)
  private String execId;
  @Column(precision = 15, scale = 2)
  private BigDecimal price;
  @Column(nullable = false)
  private Double filledQuantity;
  @Column(precision = 15, scale = 2)
  private BigDecimal commission;
  @Column(precision = 15, scale = 2)
  private BigDecimal totalAmount;
  @Column(precision = 15, scale = 2)
  private BigDecimal realizedPnl;
  @Column(nullable = false, length = 3)
  private String currency;
  @CreationTimestamp
  @Column(nullable = false)
  private LocalDateTime createdAt;
  @UpdateTimestamp
  @Column(nullable = false)
  private LocalDateTime updatedAt;
  private String errorMessage;
}
