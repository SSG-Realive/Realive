package com.realive.domain.customer;

import com.realive.domain.product.Product;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "wishlists")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Wishlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    protected LocalDateTime created;

    public Wishlist(Customer customer, Product product) {
        this.customer = customer;
        this.product = product;
    }
    
}
