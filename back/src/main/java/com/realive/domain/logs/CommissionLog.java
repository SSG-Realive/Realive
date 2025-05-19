package com.realive.domain.logs;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "commission_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommissionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "sales_log_id")
    private Integer salesLogId;

    @Column(name = "commission_rate", precision = 5, scale = 2)
    private BigDecimal commissionRate;

    @Column(name = "commission_amount")
    private Integer commissionAmount;

    @Column(name = "recorded_at")
    private LocalDateTime recordedAt;
}
