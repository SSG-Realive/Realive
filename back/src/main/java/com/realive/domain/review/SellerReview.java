package com.realive.domain.review;

import com.realive.domain.common.BaseTimeEntity;
import com.realive.domain.customer.Customer;
import com.realive.domain.order.Order;
import com.realive.domain.seller.Seller;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
// uniqueConstraints 추가, 테이블 수준에서 중복을 방지하는 역할
@Table(name = "seller_reviews", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"order_id", "customer_id", "seller_id"}) // 이 3개 컬럼의 조합이 유일해야 함
})
public class SellerReview extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;

    @Column(nullable = false)
    private double rating;

    @Column(nullable = false, length = 3000)
    private String content;

    @Builder.Default
    @Column(nullable = false, name = "is_hidden")
    private boolean isHidden = false;

}
