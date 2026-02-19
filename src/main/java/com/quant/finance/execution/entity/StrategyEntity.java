package com.quant.finance.execution.entity;

import static jakarta.persistence.EnumType.STRING;

import com.quant.finance.execution.model.StrategyType;
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
@Table(name = "strategy")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StrategyEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;
  @Column(nullable = false, length = 200)
  private String name;
  @Column(length = 500)
  private String description;
  @Enumerated(STRING)
  @Column(nullable = false, length = 20)
  private StrategyType type;
  @Column(nullable = false)
  private Double maxPositionQuantity;
  @Column(nullable = false, precision = 15, scale = 2)
  private BigDecimal maxPositionAmount;
  @Column(nullable = false)
  private Integer takeProfitPercentage;
  @Column(nullable = false)
  private Integer stopLossPercentage;
  @CreationTimestamp
  @Column(nullable = false)
  private LocalDateTime createdAt;
  @UpdateTimestamp
  @Column(nullable = false)
  private LocalDateTime updatedAt;
}
