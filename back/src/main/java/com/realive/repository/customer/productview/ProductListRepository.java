package com.realive.repository.customer.productview;

import java.util.List;

import com.realive.dto.product.ProductListDTO;

// [Customer] 상품 목록 조회 Repository

public interface ProductListRepository {

    // 상품ID리스트로 목록 조회 정보 가져오기
    List<ProductListDTO> getWishlistedProducts(List<Long> productIds);

}
