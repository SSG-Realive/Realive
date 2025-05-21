package com.realive.domain.order;

//import com.realive.domain.customer.Customer;
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
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔗 상품 정보
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // 🔗 구매자 정보 (선택)
    // @ManyToOne(fetch = FetchType.LAZY)
    //@JoinColumn(name = "customer_id", nullable = false)
    //private Customer customer;

    // 주문 수량, 결제 금액 등도 필요시 추가
}