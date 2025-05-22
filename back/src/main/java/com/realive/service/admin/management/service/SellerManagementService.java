package com.realive.service.admin.management.service;

import com.realive.dto.admin.management.ProductDTO;
import com.realive.dto.admin.management.SellerDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * 판매자 관리 서비스
 */
public interface SellerManagementService {
    // 판매자 목록 조회 (페이징)
    Page<SellerDTO> getSellers(Pageable pageable);

    // 특정 조건으로 판매자 검색
    Page<SellerDTO> searchSellers(String keyword, Pageable pageable);

    // 판매자 상세 정보 조회
    SellerDTO getSellerById(Integer sellerId);

    // 판매자 상태 업데이트
    SellerDTO updateSellerStatus(Integer sellerId, String status);

    // 판매자 커미션 업데이트
    SellerDTO updateSellerCommission(Integer sellerId, BigDecimal commission);

    // 판매자 상품 목록 조회
    Page<ProductDTO> getSellerProducts(Integer sellerId, Pageable pageable);

    // 판매자 매출 통계 조회
    Map<String, Object> getSellerSalesStatistics(Integer sellerId, LocalDate startDate, LocalDate endDate);

    // 판매자 승인 처리
    SellerDTO approveSeller(Integer sellerId, boolean approved, String message);
}