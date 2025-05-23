package com.realive.domain.order;

import com.realive.domain.customer.Customer;
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
    @Column(nullable =  false, length = 100)
    private String status;
    @Column(name = "total_price", nullable =  false, length = 100)
    private Long totalPrice;
    @Column(name = "delivery_address", nullable =  false, length = 500)
    private String deliveryAddress;
    @Column(name = "ordered_at", nullable =  false, updatable = false, length = 100)
    private LocalDateTime OrderedAt;
    @Column(name = "updated_at", nullable =  false, length = 100)
    private LocalDateTime UpdatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customers_id", nullable = false)
    private Customer customer;
}
