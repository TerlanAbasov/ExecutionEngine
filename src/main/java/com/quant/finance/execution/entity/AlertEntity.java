package com.quant.finance.execution.entity;

import static jakarta.persistence.EnumType.STRING;

import com.quant.finance.execution.enums.OrderAction;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "alert")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(nullable = false, length = 32)
  private String symbol;
  @Enumerated(STRING)
  @Column(nullable = false, length = 20)
  private OrderAction action;
  @Column(nullable = false, length = 200)
  private String strategy;
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

  @OneToMany(mappedBy = "alert", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @Setter(AccessLevel.NONE)
  private List<OrderEntity> orders = new ArrayList<>();

  public void addOrder(OrderEntity order) {
    if (order == null) {
      return;
    }

    this.orders.add(order);
    order.setAlert(this);
  }
}
