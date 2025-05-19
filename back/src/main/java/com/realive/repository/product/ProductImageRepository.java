package com.realive.repository.product;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.realive.domain.product.ProductImage;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    
    List<ProductImage> findByProductId(Long productId);
    
}
