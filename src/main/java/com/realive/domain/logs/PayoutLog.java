package com.realive.domain.logs;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "payout_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PayoutLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "seller_id")
    private Integer sellerId;

    @Column(name = "period_start")
    private LocalDate periodStart;

    @Column(name = "period_end")
    private LocalDate periodEnd;

    @Column(name = "total_sales")
    private Integer totalSales;

    @Column(name = "total_commission")
    private Integer totalCommission;

    @Column(name = "payout_amount")
    private Integer payoutAmount;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

}
