package com.realive.domain.payment;

import com.realive.domain.common.BaseTimeEntity;
import com.realive.domain.common.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "auction_payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuctionPayment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "auction_id", nullable = false)
    private Integer auctionId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "payment_key", nullable = false, unique = true)
    private String paymentKey;

    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Column(name = "receiver_name", nullable = false)
    private String receiverName;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "delivery_address", nullable = false)
    private String deliveryAddress;

    @Column(name = "payment_method", nullable = false)
    private String paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.READY;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "order_id")
    private Long orderId; // 결제 완료 후 생성된 주문 ID
} 