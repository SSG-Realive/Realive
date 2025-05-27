package com.realive.service.customer;
import java.util.Optional;

import com.realive.dto.page.PageRequestDTO;
import com.realive.dto.page.PageResponseDTO;
import com.realive.dto.product.ProductListDTO;
import com.realive.dto.product.ProductResponseDTO;

// 상품 조회 서비스

public interface ProductViewService {
    
    // 상품 목록 검색
    PageResponseDTO<ProductListDTO> search(PageRequestDTO dto, Long categoryId);

    // 상품 상세 조회
    ProductResponseDTO getProductDetail(Long id);

}

