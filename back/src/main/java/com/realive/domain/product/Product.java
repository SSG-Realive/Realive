package com.realive.domain.product;

import com.realive.domain.common.BaseTimeEntity;
import com.realive.domain.common.enums.ProductStatus;
import com.realive.domain.seller.Seller;
import jakarta.persistence.*;
import lombok.*;

/**
 * 상품 도메인 엔티티
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "products")
public class Product extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 상품명
    @Column(nullable = false, length = 100)
    private String name;

    // 상품 설명
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    // 가격
    @Column(nullable = false)
    private int price;

    // 재고 수량
    @Builder.Default
    @Column(nullable = false)
    private int stock = 1;

    // 가구 크기 (cm)
    private Integer width;
    private Integer depth;
    private Integer height;

    // 상태 (enum)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ProductStatus status = ProductStatus.상;

    // 판매 여부 (true = 판매중)
    @Builder.Default
    @Column(nullable = false, name = "is_active")
    private boolean active = true;

   
    // 판매자Z
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;

    // 카테고리
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

}

