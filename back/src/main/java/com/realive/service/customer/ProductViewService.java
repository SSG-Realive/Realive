package com.realive.service.customer;

import com.realive.dto.page.PageRequestDTO;
import com.realive.dto.page.PageResponseDTO;
import com.realive.dto.product.ProductListDTO;
import com.realive.dto.product.ProductResponseDTO;


public interface ProductViewService {
    
    PageResponseDTO<ProductListDTO> search(PageRequestDTO dto, Long categoryId);

    ProductResponseDTO getProductDetail(Long id);

}

