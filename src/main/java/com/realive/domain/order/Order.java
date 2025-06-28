package com.realive.domain.order;

import com.realive.domain.common.enums.OrderStatus;
import com.realive.domain.customer.Customer;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @Column(name = "total_price", nullable =  false)
    private int totalPrice;

    @Column(name = "delivery_address", nullable =  false, length = 500)
    private String deliveryAddress;

    @Column(name = "ordered_at", nullable =  false, updatable = false)
    private LocalDateTime orderedAt;

    @Column(name = "updated_at", nullable =  false)
    private LocalDateTime updatedAt;

    // 새로운 필드: 결제 방식
    @Column(name = "payment_method", nullable = false, length = 50)
    private String paymentMethod; // 예를 들어 "CARD", "CASH", "BANK_TRANSFER" 등

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();
}