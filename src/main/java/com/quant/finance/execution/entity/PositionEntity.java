package com.quant.finance.execution.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

//@Entity
//@Table(name = "position")
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
public class PositionEntity {
  //@Id
  //@GeneratedValue(strategy = GenerationType.IDENTITY)
  //private Integer id;
  //@Column(nullable = false, length = 50)
  //private String accountId;
  //@Column(nullable = false, length = 50)
  //private String symbol;
  //@Column(nullable = false, length = 20)
  //private String assetType;
  //@Column(nullable = false, length = 20)
  //private String exchange;
  //@Column(nullable = false, length = 10)
  //private String curreny;
  //@Column(nullable = false)
  //private Integer contractId;
  //
  //todo change data types
  //private Double quantity = 0d;
  //private Double averageCost;
  //private String dailyPnL;
  //private String unrealizedPnl;
  //private String realizedPnl;
  //private Double value;
  //
  //@CreationTimestamp
  //@Column(nullable = false)
  //private LocalDateTime createdAt;
  //@UpdateTimestamp
  //@Column(nullable = false)
  //private LocalDateTime updatedAt;
}
