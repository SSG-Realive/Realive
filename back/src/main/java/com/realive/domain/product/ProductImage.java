package com.realive.domain.product;

import com.realive.domain.common.enums.MediaType;

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
@Table(name = "product_images")
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

     // 어떤 상품에 속하는 이미지인지 (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;


    // 이미지 또는 영상 경로
    @Column(nullable = false)
    private String url;

    // 대표 이미지 여부 (썸네일)
    @Builder.Default
    @Column(name = "is_thumbnail", nullable = false)
    private boolean isThumbnail = false;

    // IMAGE / VIDEO
    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false, length = 20)
    private MediaType mediaType; 

   
    
    }
