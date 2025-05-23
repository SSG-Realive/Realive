package com.realive.service.admin.management.service;

import com.realive.dto.admin.management.OrderDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Map;

/**
 * 주문 관리 서비스
 */
public interface OrderManagementService {
    // 주문 목록 조회 (페이징)
    Page<OrderDTO> getOrders(Pageable pageable);

    // 특정 조건으로 주문 검색
    Page<OrderDTO> searchOrders(Map<String, Object> searchParams, Pageable pageable);

    // 주문 상세 정보 조회
    OrderDTO getOrderById(Integer orderId);

    // 주문 상태 업데이트
    OrderDTO updateOrderStatus(Integer orderId, String status);

    // 주문 취소 처리
    OrderDTO cancelOrder(Integer orderId, String reason);

    // 특정 기간 주문 통계 조회
    Map<String, Object> getOrderStatistics(LocalDate startDate, LocalDate endDate);

    // 특정 고객의 주문 조회
    Page<OrderDTO> getCustomerOrders(Integer customerId, Pageable pageable);

    // 특정 판매자의 주문 조회
    Page<OrderDTO> getSellerOrders(Integer sellerId, Pageable pageable);
}