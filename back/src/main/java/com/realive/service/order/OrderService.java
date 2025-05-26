package com.realive.service.order;

import com.realive.dto.order.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    // 단일 주문 상세 조회
    OrderResponseDTO getOrder(Long orderId, Long customerId);

    // 주문 목록 조회 (페이징 포함)
    Page<OrderResponseDTO> getOrderList(Pageable pageable);

    // 구매내역 삭제
    void deleteOrder(OrderDeleteRequestDTO orderDeleteRequestDTO);

    // 구매 취소
    void cancelOrder(OrderCancelRequestDTO orderCancelRequestDTO);

    // 구매 확정
    void confirmOrder(OrderConfirmRequestDTO orderConfirmRequestDTO);

    // 결제 진행 + 구매내역 생성
    Long processPayment(PayRequestDTO payRequestDTO);
}