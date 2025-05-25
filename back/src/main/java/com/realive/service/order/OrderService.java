package com.realive.service.order;

import com.realive.dto.order.OrderDeleteRequestDTO;
import com.realive.dto.order.OrderResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.realive.dto.order.OrderAddRequestDTO;

public interface OrderService {
    // 단일 주문 상세 조회
    OrderResponseDTO getOrder(Long orderId, Long customerId);

    // 주문 목록 조회 (페이징 포함)
    Page<OrderResponseDTO> getOrderList(Pageable pageable);

    // 주문 생성
    Long createOrder(OrderAddRequestDTO orderAddRequestDTO);

    // 구매내역 삭제
    void deleteOrder(OrderDeleteRequestDTO orderDeleteRequestDTO);
}