package com.realive.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.realive.dto.product.ProductListDto;
import com.realive.dto.product.ProductResponseDto;

public interface ProductViewService {

    Page<ProductListDto> getProductList(Pageable pageable);

    ProductResponseDto getProductDetail(Long id);
    
}
