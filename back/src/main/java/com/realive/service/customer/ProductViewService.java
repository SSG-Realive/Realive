package com.realive.service.customer;

import com.realive.dto.page.PageRequestDTO;
import com.realive.dto.page.PageResponseDTO;
import com.realive.dto.product.ProductListDTO;
import com.realive.dto.product.ProductResponseDTO;

// [Customer] 상품 조회 Service

public interface ProductViewService {
    
    // 상품 목록 검색
    PageResponseDTO<ProductListDTO> search(PageRequestDTO dto, Long categoryId);

    // 상품 상세 조회
    ProductResponseDTO getProductDetail(Long id);

}

