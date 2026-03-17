package com.quant.finance.execution.entity;

import static jakarta.persistence.EnumType.STRING;

import com.ib.client.OrderType;
import com.quant.finance.execution.enums.PositionType;
import com.quant.finance.execution.enums.StrategyType;
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
  @Enumerated(STRING)
  @Column(nullable = false, length = 20)
  private StrategyType type;
  @Enumerated(STRING)
  @Column(nullable = false, length = 20)
  private PositionType positionType;
  @Enumerated(STRING)
  @Column(nullable = false, length = 20)
  private OrderType buyOrderType;
  @Column(nullable = false, precision = 4, scale = 3)
  private BigDecimal buyLimitCeiling;
  @Enumerated(STRING)
  @Column(nullable = false, length = 20)
  private OrderType sellOrderType;
  @Column(nullable = false, precision = 5, scale = 3)
  private BigDecimal sellLimitFloor;
  @Column(nullable = false, precision = 15, scale = 2)
  private BigDecimal maxPositionAmount;
  @Column(nullable = false)
  private Integer takeProfitPercentage;
  @Column(nullable = false)
  private Integer stopLossPercentage;
  //@Column(nullable = false)
  //private boolean enabled;
  @Column(length = 500)
  private String description;
  @CreationTimestamp
  @Column(nullable = false)
  private LocalDateTime createdAt;
  @UpdateTimestamp
  @Column(nullable = false)
  private LocalDateTime updatedAt;
}
