package com.realive.repository.customer.productview;

import com.realive.dto.page.PageRequestDTO;
import com.realive.dto.page.PageResponseDTO;
import com.realive.dto.product.ProductListDTO;

// [Customer] 상품 검색 Repository

public interface ProductSearch {

    // 검색 및 카테고리ID로 필터링된 상품 목록 가져오기
    PageResponseDTO<ProductListDTO> search(PageRequestDTO requestDTO, Long categoryId);
}
