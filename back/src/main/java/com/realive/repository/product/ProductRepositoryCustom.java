package com.realive.repository.product;

import com.realive.domain.product.Product;
import com.realive.dto.product.ProductSearchCondition;
import org.springframework.data.domain.Page;

/**
 * ProductRepositoryCustom
 * - QueryDSL을 활용한 동적 상품 검색 기능을 정의하는 사용자 정의 리포지토리 인터페이스
 * - 복잡한 조건 기반의 상품 조회를 위해 커스텀 메서드 선언
 */
public interface ProductRepositoryCustom {

    /**
     * 판매자의 상품 목록을 조건에 따라 검색
     *
     * @param condition 검색 조건 (카테고리, 가격, 상태, 키워드 등)
     * @param sellerId 현재 로그인한 판매자 ID
     * @return 조건에 맞는 상품 목록 (페이징 처리된 결과)
     */
    Page<Product> searchProducts(ProductSearchCondition condition, Long sellerId);
}