package com.realive.service.productview;
import java.util.Optional;

import com.realive.dto.page.PageRequestDTO;
import com.realive.dto.page.PageResponseDTO;
import com.realive.dto.product.ProductListDto;
import com.realive.dto.product.ProductResponseDto;


public interface ProductViewService {
    
    PageResponseDTO<ProductListDto> search(PageRequestDTO dto, Long categoryId);

    ProductResponseDto getProductDetail(Long id);

}

