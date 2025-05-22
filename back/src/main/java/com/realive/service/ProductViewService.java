package com.realive.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.realive.dto.product.ProductListDTO;
import com.realive.dto.product.ProductResponseDTO;

public interface ProductViewService {

    Page<ProductListDTO> getProductList(Pageable pageable);

    ProductResponseDTO getProductDetail(Long id);
    
}
