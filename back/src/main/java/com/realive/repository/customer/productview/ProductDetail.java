package com.realive.repository.customer.productview;

import java.util.Optional;

import com.realive.dto.product.ProductResponseDto;

// 상품 상세 조회 레포지토리

public interface ProductDetail {
    Optional<ProductResponseDto> findProductDetailById(Long id);
}
