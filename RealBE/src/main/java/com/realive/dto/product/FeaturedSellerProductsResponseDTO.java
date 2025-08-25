package com.realive.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 추천용: 선택된 판매자와 그 판매자의 랜덤 상품 리스트를 담은 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeaturedSellerProductsResponseDTO {
    private Long sellerId;
    private String sellerName;
    private List<FeaturedProductSummaryResponseDTO> products;
}
