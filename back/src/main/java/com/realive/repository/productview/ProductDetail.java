package com.realive.repository.productview;

import java.util.Optional;

import com.realive.dto.product.ProductResponseDTO;

public interface ProductDetail {
    Optional<ProductResponseDTO> findProductDetailById(Long id);
}
