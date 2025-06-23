package com.realive.service.order;

import com.realive.dto.order.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    // 단일 주문 상세 조회
    OrderResponseDTO getOrder(Long orderId, Long customerId);

    // 주문 목록 조회 (페이징 포함)
    Page<OrderResponseDTO> getOrderList(Pageable pageable, Long customerId);

    // 구매내역 삭제
    void deleteOrder(OrderDeleteRequestDTO orderDeleteRequestDTO);

    // 구매 취소
    void cancelOrder(OrderCancelRequestDTO orderCancelRequestDTO);

    // 구매 확정
    void confirmOrder(OrderConfirmRequestDTO orderConfirmRequestDTO);

    // 단일 상품 바로 구매 결제 진행 및 구매내역 생성 (여전히 OrderService의 책임)
    Long processDirectPayment(PayRequestDTO payRequestDTO);

    /**
     * 단일 상품 바로 구매 정보 조회
     * @param productId 상품 ID
     * @param quantity 수량
     * @return DirectPaymentInfoDTO
     */
    DirectPaymentInfoDTO getDirectPaymentInfo(Long productId, Integer quantity);

    // TODO: 장바구니 결제 처리 후 최종 주문 생성 로직을 CartService에서 호출할 수 있도록
    //       새로운 private/protected 또는 패키지-private 메서드를 추가하거나,
    //       아니면 CartService에서 OrderService의 `processDirectPayment`와 유사한 방식으로
    //       직접 Order를 생성하는 로직을 구현하도록 변경할 수 있습니다.
    //       여기서는 CartService가 직접 Order를 생성하는 방식으로 변경합니다.
    //       -> 즉, processCartPayment는 CartService로 완전히 이동합니다.
}