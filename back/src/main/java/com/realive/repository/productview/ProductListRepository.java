package com.realive.repository.productview;

import java.util.List;

import com.realive.dto.product.ProductListDto;

public interface ProductListRepository {
    List<ProductListDto> getWishlistedProducts(List<Long> productIds);
}
