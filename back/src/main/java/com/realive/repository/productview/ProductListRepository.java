package com.realive.repository.productview;

import java.util.List;

import com.realive.dto.product.ProductListDTO;

public interface ProductListRepository {
    List<ProductListDTO> getWishlistedProducts(List<Long> productIds);
}
