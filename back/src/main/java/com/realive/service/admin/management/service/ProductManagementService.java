package com.realive.service.admin.management.service;

import com.realive.dto.admin.management.ProductDTO;
import com.realive.dto.admin.management.ReviewDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 상품 관리 서비스
 */
public interface ProductManagementService {
    // 상품 목록 조회 (페이징)
    Page<ProductDTO> getProducts(Pageable pageable);

    // 특정 조건으로 상품 검색
    Page<ProductDTO> searchProducts(Map<String, Object> searchParams, Pageable pageable);

    // 상품 상세 정보 조회
    ProductDTO getProductById(Integer productId);

    // 상품 상태 업데이트
    ProductDTO updateProductStatus(Integer productId, String status);

    // 상품 가격 업데이트
    ProductDTO updateProductPrice(Integer productId, BigDecimal price);

    // 상품 재고 업데이트
    ProductDTO updateProductInventory(Integer productId, Integer inventory);

    // 상품 승인 처리
    ProductDTO approveProduct(Integer productId, boolean approved, String message);

    // 상품 리뷰 조회
    Page<ReviewDTO> getProductReviews(Integer productId, Pageable pageable);

    // 상품 통계 정보 조회
    Map<String, Object> getProductStatistics(Integer productId);
}