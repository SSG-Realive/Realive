package com.realive.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 상품 상세 조회 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponseDTO {

    private Long id;                  // 상품 ID
    private String name;              // 상품명
    private String description;       // 설명
    private int price;                // 가격
    private int stock;                // 재고
    private Integer width;            // 가로
    private Integer depth;            // 세로
    private Integer height;           // 높이
    private String status;            // 상태 (enum → String)
    private boolean isActive;         // 판매 여부
    private String imageThumbnailUrl; // 대표 이미지 썸네일 (필수)
    private String videoThumbnailUrl; // 대표 영상 썸네일 (선택)
    private String categoryName;      // 카테고리 이름 (선택)
    private String sellerName;        // 판매자 이름 (선택)

    private DeliveryPolicyDTO deliveryPolicy;  // 배송정책
}