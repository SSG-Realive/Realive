package com.realive.repository.customer.productview;

import java.util.List;

import com.realive.dto.product.ProductListDto;

// 상품 목록 조회 레포지토리

public interface ProductListRepository {
    List<ProductListDto> getWishlistedProducts(List<Long> productIds);
}
