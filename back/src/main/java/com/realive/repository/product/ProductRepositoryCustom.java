package com.realive.repository.product;

import com.realive.domain.product.Product;
import com.realive.dto.product.ProductSearchCondition;

import java.util.List;

public interface ProductRepositoryCustom {
    List<Product> searchProducts(ProductSearchCondition condition, Long sellerId);
}