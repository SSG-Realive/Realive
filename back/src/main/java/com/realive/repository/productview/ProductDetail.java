package com.realive.repository.productview;

import java.util.Optional;

import com.realive.dto.product.ProductResponseDto;

public interface ProductDetail {
    Optional<ProductResponseDto> findProductDetailById(Long id);
}
