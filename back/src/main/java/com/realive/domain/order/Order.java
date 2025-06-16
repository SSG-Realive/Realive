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
    private LocalDateTime OrderedAt;

    @Column(name = "updated_at", nullable =  false)
    private LocalDateTime UpdatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customers_id", nullable = false)
    private Customer customer;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();
}