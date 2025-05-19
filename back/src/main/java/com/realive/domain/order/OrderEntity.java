package com.realive.domain.order;

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
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable =  false, length = 100)
    private String status;
    @Column(name = "total_price", nullable =  false, length = 100)
    private Long totalPrice;
    @Column(name = "delivery_address", nullable =  false, length = 500)
    private String deliveryAddress;
    @Column(name = "Ordered_at", nullable =  false, updatable = false, length = 100)
    private LocalDateTime OrderedAt;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "customers_id", nullable = false)
//    private CustomerEntity customer;
}
