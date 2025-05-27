package com.realive.repository.productview;



import com.realive.dto.page.PageRequestDTO;
import com.realive.dto.page.PageResponseDTO;
import com.realive.dto.product.ProductListDTO;

public interface ProductSearch {
    PageResponseDTO<ProductListDTO> search(PageRequestDTO requestDTO, Long categoryId);
}
