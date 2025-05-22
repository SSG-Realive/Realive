package com.realive.service.admin.management.service;

import com.realive.dto.admin.management.CustomerDTO;
import com.realive.dto.admin.management.OrderDTO;
import com.realive.dto.admin.management.ProductDTO;
import com.realive.dto.admin.management.SellerDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 관리자 대시보드 서비스
 */
public interface AdminDashboardService {
    // 대시보드 요약 정보 조회
    Map<String, Object> getDashboardSummary();

    // 일간/주간/월간 매출 통계 조회
    Map<String, Object> getSalesStatistics(String period, LocalDate startDate, LocalDate endDate);

    // 신규 가입 고객 통계 조회
    List<CustomerDTO> getNewCustomers(int limit);

    // 인기 상품 목록 조회
    List<ProductDTO> getTopSellingProducts(int limit);

    // 우수 판매자 목록 조회
    List<SellerDTO> getTopSellers(int limit);

    // 최근 주문 목록 조회
    List<OrderDTO> getRecentOrders(int limit);

    // 승인 대기 중인 판매자 목록 조회
    List<SellerDTO> getPendingSellers();

    // 승인 대기 중인 상품 목록 조회
    List<ProductDTO> getPendingProducts();
}