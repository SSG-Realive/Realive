package com.realive.service.admin.management.service;

import com.realive.dto.admin.management.CustomerDTO;
import com.realive.dto.admin.management.OrderDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

/**
 * 고객 관리 서비스
 */
public interface CustomerManagementService {
    // 고객 목록 조회 (페이징)
    Page<CustomerDTO> getCustomers(Pageable pageable);

    // 특정 조건으로 고객 검색
    Page<CustomerDTO> searchCustomers(String keyword, Pageable pageable);

    // 고객 상세 정보 조회
    CustomerDTO getCustomerById(Integer customerId);

    // 고객 상태 업데이트
    CustomerDTO updateCustomerStatus(Integer customerId, String status);

    // 고객 주문 이력 조회
    Page<OrderDTO> getCustomerOrders(Integer customerId, Pageable pageable);

    // 고객 통계 정보 조회
    Map<String, Object> getCustomerStatistics(Integer customerId);
}