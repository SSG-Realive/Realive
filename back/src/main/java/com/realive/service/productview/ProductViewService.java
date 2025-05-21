package com.realive.service.productview;



import com.realive.dto.page.PageRequestDTO;
import com.realive.dto.page.PageResponseDTO;
import com.realive.dto.product.ProductListDto;




public interface ProductViewService {
    
    PageResponseDTO<ProductListDto> search(PageRequestDTO dto, Long categoryId);
}

