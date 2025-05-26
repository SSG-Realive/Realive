package com.realive.domain.order;

import com.realive.domain.customer.Customer;
import com.realive.domain.common.enums.DeliveryStatus;
import com.realive.domain.common.enums.OrderStatus;
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
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable =  false, length = 50)
    private OrderStatus status; // 주문 자체의 상태

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status", nullable = false, length = 50) // 배송 상태
    private DeliveryStatus deliveryStatus;

    @Column(name = "total_price", nullable =  false)
    private int totalPrice;

    @Column(name = "delivery_address", nullable =  false, length = 500)
    private String deliveryAddress;

    @Column(name = "receiver_name", nullable = false, length = 100)
    private String receiverName;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "payment_type", nullable = false, length = 50)
    private String paymentType;

    @Column(name = "ordered_at", nullable =  false, updatable = false)
    private LocalDateTime OrderedAt;

    @Column(name = "updated_at", nullable =  false)
    private LocalDateTime UpdatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customers_id", nullable = false)
    private Customer customer;

    // 추가: 이 주문에 대한 판매자 리뷰 작성 여부 플래그
    @Column(name = "is_seller_review_written", nullable = false)
    private boolean isSellerReviewWritten = false; // 기본값은 false
}