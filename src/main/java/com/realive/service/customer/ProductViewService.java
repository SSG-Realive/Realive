package com.realive.service.customer;

import com.realive.dto.page.PageRequestDTO;
import com.realive.dto.page.PageResponseDTO;
import com.realive.dto.product.ProductListDTO;
import com.realive.dto.product.ProductResponseDTO;

import java.util.List;

// [Customer] 상품 조회 Service

public interface ProductViewService {
    
    // 상품 목록 검색
    PageResponseDTO<ProductListDTO> search(PageRequestDTO dto, Long categoryId);

    // 상품 상세 조회
    ProductResponseDTO getProductDetail(Long id);

    // 관련 상품 추천
    List<ProductListDTO> getRelatedProducts(Long productId);

    // 찜 많은 인기 상품 조회
    List<ProductListDTO> getPopularProducts();

}

