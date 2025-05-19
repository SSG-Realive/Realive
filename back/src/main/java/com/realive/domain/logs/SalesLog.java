package com.realive.domain.logs;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sales_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SalesLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "order_item_id")
    private Integer orderItemId;

    @Column(name = "product_id")
    private Integer productId;

    @Column(name = "seller_id")
    private Integer sellerId;

    @Column(name = "customer_id")
    private Integer customerId;    

    private Integer quantity;

    @Column(name = "unit_price")
    private Integer unitPrice;

    @Column(name = "total_price")
    private Integer totalPrice;
    
    @Column(name = "sold_at")
    private LocalDate soldAt;
}
