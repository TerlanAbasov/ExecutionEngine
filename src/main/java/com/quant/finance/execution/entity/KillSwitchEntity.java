package com.quant.finance.execution.entity;

import com.quant.finance.execution.enums.KillSwitchAction;
import com.quant.finance.execution.enums.KillSwitchReason;
import com.quant.finance.execution.enums.KillSwitchScope;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

//@Entity
//@Table(name = "kill_switch")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KillSwitchEntity {

  //@Id
  //@GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  //@Enumerated(EnumType.STRING)
  //@Column(nullable = false, length = 20)
  private KillSwitchScope scope;

  //@Column(length = 100)
  private String scopeValue;

  //@Column(nullable = false)
  private boolean isActive;

  //@Enumerated(EnumType.STRING)
  //@Column(nullable = false, length = 30)
  private KillSwitchReason reason;

  //@Column(length = 255)
  private String reasonDetail;

  private BigDecimal maxDailyLoss;

  private Integer maxOrderCount;

  private Integer maxErrorCount;

  private Integer orderWindowMinutes;

  //@Enumerated(EnumType.STRING)
  //@Column(nullable = false, length = 30)
  private KillSwitchAction triggerAction;

  private boolean closePositionsOnKill;

  private boolean cancelOpenOrdersOnKill;

  private LocalTime scheduledStart;

  private LocalTime scheduledEnd;

  //@CreationTimestamp
  //@Column(nullable = false)
  private LocalDateTime createdAt;
  //@UpdateTimestamp
  //@Column(nullable = false)
  private LocalDateTime updatedAt;
}