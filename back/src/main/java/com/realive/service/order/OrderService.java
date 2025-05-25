package com.realive.service.order;

import com.realive.dto.order.OrderListResponseDTO; // 사용되지 않을 수 있으나, 임시 유지
import com.realive.dto.order.OrderResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.realive.dto.order.OrderAddRequestDTO; // createOrder를 위해 추가

public interface OrderService {
    // 단일 주문 상세 조회
    OrderResponseDTO getOrder(Long orderId, Long customerId);

    // 주문 목록 조회 (페이징 포함)
    Page<OrderResponseDTO> getOrderList(Pageable pageable);

    // 주문 생성
    Long createOrder(OrderAddRequestDTO orderAddRequestDTO); // 인터페이스에 이 메서드가 명확히 있어야 함
}