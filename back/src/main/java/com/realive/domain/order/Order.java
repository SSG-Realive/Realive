package com.realive.domain.order;

//import com.realive.domain.customer.Customer;
import com.realive.domain.common.BaseTimeEntity;
import com.realive.domain.product.Product;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "orders")  // 테이블명이 order일 경우 예약어 주의
public class Order extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔗 상품 정보
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

}