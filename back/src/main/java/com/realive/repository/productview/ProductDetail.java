package com.realive.repository.productview;

import java.util.Optional;

import com.realive.dto.productview.ProductResponseDto;

public interface ProductDetail {
    Optional<ProductResponseDto> findProductDetailById(Long id);
}
