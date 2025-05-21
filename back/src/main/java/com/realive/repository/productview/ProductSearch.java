package com.realive.repository.productview;



import com.realive.dto.page.PageRequestDTO;
import com.realive.dto.page.PageResponseDTO;
import com.realive.dto.product.ProductListDto;

public interface ProductSearch {
    PageResponseDTO<ProductListDto> search(PageRequestDTO requestDTO, Long categoryId);
}
