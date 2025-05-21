package com.realive.dto.productview;

import lombok.AllArgsConstructor;
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
public class ProductListDto {

    private Long id;          // 상품 ID
    private String name;      // 상품명
    private int price;        // 가격
    private String status;    // 상태 (상, 중, 하)
    private boolean isActive; // 판매 여부
    private String thumbnailUrl;

   
	
    public ProductListDto(Long id, String name, int price, String status, boolean isActive, String thumbnailUrl) {
		this.id = id;
		this.name = name;
		this.price = price;
		this.status = status;
		this.isActive = isActive;
		this.thumbnailUrl = thumbnailUrl;
	}
}