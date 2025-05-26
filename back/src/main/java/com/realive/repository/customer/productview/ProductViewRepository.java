package com.realive.repository.customer.productview;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.realive.domain.product.Product;

// 상품 조회 레포지토리

public interface ProductViewRepository extends JpaRepository<Product, Long>, ProductSearch, ProductDetail {

    // 상품 id로 상품 찾기
    // wishlist의 경우 판매 중지 상품도 조회 가능하므로 판매 여부는 조건에서 제외.
    Optional<Product> findById(Long id);


}