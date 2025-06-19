package com.realive.dto.product;

import com.realive.domain.common.enums.ProductStatus;
import com.realive.dto.page.PageRequestDTO;

import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * 상품 검색/필터 조건 DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProductSearchCondition extends PageRequestDTO{

    private Long categoryId;
    private ProductStatus status;
    private Integer minPrice;
    private Integer maxPrice;
    private Boolean isActive;
}