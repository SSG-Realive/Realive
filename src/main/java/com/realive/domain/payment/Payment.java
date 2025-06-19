package com.realive.domain.payment;

import com.realive.domain.common.BaseTimeEntity;
import com.realive.domain.common.enums.PaymentStatus; // 새로 만든 PaymentStatus 임포트
import com.realive.domain.order.Order;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "payments")
public class Payment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_key", nullable = false, unique = true, length = 255)
    private String paymentKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private Long amount;

    @Column(name = "balance_amount")
    private Long balanceAmount;

    @Column(name = "method", nullable = false, length = 50)
    private String method;

    @Enumerated(EnumType.STRING) // Enum으로 변경
    @Column(nullable = false, length = 50)
    private PaymentStatus status; // String에서 PaymentStatus로 변경!

    @Column(name = "requested_at", nullable = false, updatable = false)
    private LocalDateTime requestedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "type", nullable = false, length = 50)
    private String type;

    @Column(name = "customer_key", length = 255)
    private String customerKey;

    @Column(nullable = false, length = 10)
    private String currency;

    @Column(name = "last_transaction_key", length = 255)
    private String lastTransactionKey;

    @Column(name = "raw_response_data", columnDefinition = "jsonb")
    private String rawResponseData;

    @PrePersist
    protected void createdAt() {
        super.getCreatedAt();
        if (this.currency == null) {
            this.currency = "KRW";
        }
    }

    @PreUpdate
    protected void updatedAt() {
        super.getUpdatedAt();
    }
}