package com.realive.domain.product;

import com.realive.domain.common.BaseTimeEntity;
import com.realive.domain.common.enums.DeliveryType;
import jakarta.persistence.*;
import lombok.*;

/**
 * 배송 정책 엔티티
 * 각 상품(Product)에 1:1로 연결됨
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "delivery_policy")
public class DeliveryPolicy extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 배송 방식: 무료배송 / 유료배송
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryType type;

    // 배송 비용 (유료배송일 경우 사용)
    @Column
    private int cost;

    // 지역 제한 정보
    @Column(name = "region_limit", columnDefinition = "TEXT")
    private String regionLimit;

    // 🔗 상품과 1:1 매핑
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}