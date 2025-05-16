package com.realive.domain.product;

import com.realive.domain.common.enums.DeliveryType;
import jakarta.persistence.*;
import lombok.*;

/**
 * 상품 배송 정책 엔티티
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "delivery_policy")
public class DeliveryPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 배송 방식 (enum): 무료배송 / 유료배송 / 
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryType type;

    // 배송 비용 (무료 = 0)
    private int cost = 0;

    // 지역 제한 정보 (예: 서울/경기만 가능 등)
    @Column(columnDefinition = "TEXT" , name = "region_limit")
    private String regionLimit;

    // 연결된 상품 (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}
