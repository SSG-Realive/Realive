package com.realive.dto.product;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 상품 목록 조회 DTO (간략 응답용)
 */
@Data
@NoArgsConstructor
//@AllArgsConstructor
@Builder
@Getter
@Setter
public class ProductListDTO {

    private Long id;          // 상품 ID
    private String name;      // 상품명
    private int price;        // 가격
    private String status;    // 상태 (상, 중, 하)
    private boolean isActive; // 판매 여부
    private String thumbnailUrl;
    // 검색용으로 추가
    private String categoryName; // 카테고리명
    private String sellerName;   // 판매자명
   
	
    public ProductListDTO(Long id, String name, int price, String status, boolean isActive, String thumbnailUrl, String categoryName, String sellerName) {
      this.id = id;
      this.name = name;
      this.price = price;
      this.status = status;
      this.isActive = isActive;
      this.thumbnailUrl = thumbnailUrl;
      this.categoryName = categoryName;
      this.sellerName = sellerName;
    }
}