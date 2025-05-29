package com.realive.repository.customer.productview;

import java.util.Optional;

import com.realive.dto.product.ProductResponseDTO;

// [Customer] 상품 상세 조회 Repository

public interface ProductDetail {
    
    // 상품ID로 상품 상세조회 정보 가져오기
    Optional<ProductResponseDTO> findProductDetailById(Long id);

}
