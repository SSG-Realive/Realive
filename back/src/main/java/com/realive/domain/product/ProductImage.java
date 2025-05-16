package com.realive.domain.product;

import jakarta.persistence.*;
import lombok.*;

/**
 * 상품 이미지 (여러 장 가능)
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 이미지 또는 영상 경로
    @Column(nullable = false)
    private String url;

    // 대표 이미지 여부 (썸네일)
    private boolean isThumbnail = false;

    // IMAGE / VIDEO
    @Enumerated(EnumType.STRING)
    private MediaType mediaType = MediaType.IMAGE;

    // 어떤 상품에 속하는 이미지인지 (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // IMAGE / VIDEO 구분 enum
    public enum MediaType {
        IMAGE,
        VIDEO
    }
}