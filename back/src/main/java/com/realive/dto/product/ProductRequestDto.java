package com.realive.dto.product;

import com.realive.domain.common.enums.ProductStatus;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * 상품 등록/수정 요청 DTO
 */
@Data
public class ProductRequestDto {

    private String name;                        // 상품명
    private String description;                 // 상품 설명
    private int price;                          // 가격
    private Integer stock;                      // 재고 수량 (기본 1)
    private Integer width;                      // 가로 (cm)
    private Integer depth;                      // 세로 (cm)
    private Integer height;                     // 높이 (cm)
    private ProductStatus status;               // 상태 (상, 중, 하)
    private Long categoryId;                    // 카테고리 ID
    private Boolean active;                   // 판매 여부 (기본 true)
    private MultipartFile image;                // 대표 이미지
    private DeliveryPolicyDto deliveryPolicy;   // 배송 정책 정보
}