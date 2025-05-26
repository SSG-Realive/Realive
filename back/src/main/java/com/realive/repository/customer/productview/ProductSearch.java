package com.realive.repository.customer.productview;



import com.realive.dto.page.PageRequestDTO;
import com.realive.dto.page.PageResponseDTO;
import com.realive.dto.product.ProductListDto;

// 상품 검색 레포지토리

public interface ProductSearch {
    PageResponseDTO<ProductListDto> search(PageRequestDTO requestDTO, Long categoryId);
}
