package com.realive.service.product;

import com.realive.dto.product.ProductListDto;
import com.realive.dto.product.ProductRequestDto;
import com.realive.dto.product.ProductResponseDto;

import java.util.List;

public interface ProductService {

    Long createProduct(ProductRequestDto dto, Long sellerId);

    void updateProduct(Long productId, ProductRequestDto dto, Long sellerId);

    void deleteProduct(Long productId, Long sellerId);

    List<ProductListDto> getProductsBySeller(Long sellerId);

    ProductResponseDto getProductDetail(Long productId);

    String getThumbnailUrlByProductId(Long productId);

   
}