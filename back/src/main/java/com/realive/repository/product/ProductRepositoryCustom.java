package com.realive.repository.product;

import com.realive.domain.product.Product;
import com.realive.dto.product.ProductSearchCondition;
import org.springframework.data.domain.Page;

public interface ProductRepositoryCustom {
    Page<Product> searchProducts(ProductSearchCondition condition, Long sellerId);
}