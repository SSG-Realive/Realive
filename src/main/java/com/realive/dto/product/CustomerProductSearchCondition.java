package com.realive.dto.product;

import com.realive.dto.page.PageRequestDTO;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper =  true)
public class CustomerProductSearchCondition extends PageRequestDTO{

    private Long parentCategoryId;  // 대분류 카테고리 ID
    private Long categoryId;        // 소분류 카테고리 ID
    private String sellerName;      // 판매자 이름
    private Integer minPrice;       // 최소 가격
    private Integer maxPrice;       // 최대 가격
    
}
