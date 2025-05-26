package com.realive.service.customer;
import java.util.Optional;

import com.realive.dto.page.PageRequestDTO;
import com.realive.dto.page.PageResponseDTO;
import com.realive.dto.product.ProductListDto;
import com.realive.dto.product.ProductResponseDto;

// 상품 조회 서비스

public interface ProductViewService {
    
    // 상품 목록 검색
    PageResponseDTO<ProductListDto> search(PageRequestDTO dto, Long categoryId);

    // 상품 상세 조회
    ProductResponseDto getProductDetail(Long id);

}

