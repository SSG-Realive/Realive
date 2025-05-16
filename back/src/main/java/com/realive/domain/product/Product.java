package com.realive.domain.product;

import com.realive.domain.common.BaseTimeEntity;
import com.realive.domain.common.enums.ProductStatus;
import com.realive.domain.seller.Seller;
import jakarta.persistence.*;
import lombok.*;

//import java.util.ArrayList;
//import java.util.List;

/**
 * 상품 도메인 엔티티
 * 판매자가 등록한 중고 가구 상품 정보
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    // 재고 수량 (기본값 1)
    @Column(nullable = false)
    private int stock = 1;

    // 가구 크기 정보 (단위: cm)
    private Integer width;
    private Integer depth;
    private Integer height;

    // 상품 상태 (enum): 상 / 중 / 하
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status = ProductStatus.상;

    // 판매 여부 (true = 판매중)
    @Column(nullable = false, name = "is_active")
    private boolean isActive = true;

    // 대표 이미지 1장 (대표 썸네일 경로)
    private String imageUrl;

    // 🔗 판매자 연결 (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;

    // 🔗 카테고리 연결 (N:1, 선택 가능)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

}