package com.realive.service.order;

import com.realive.dto.order.DeliveryStatusUpdateDTO;
import com.realive.dto.order.OrderDeliveryResponseDTO;

import java.util.List;

public interface OrderDeliveryService {

    /**
     * 특정 판매자의 전체 주문 배송 상태 목록 조회
     * @param sellerId 판매자 ID
     * @return 배송 상태 응답 DTO 리스트
     */
    List<OrderDeliveryResponseDTO> getDeliveriesBySeller(Long sellerId);

    /**
     * 주문 ID 기준으로 배송 상태 단건 조회 (판매자 본인만 조회 가능)
     * @param sellerId 판매자 ID
     * @param orderId 주문 ID
     * @return 배송 상태 응답 DTO
     */
    OrderDeliveryResponseDTO getDeliveryByOrderId(Long sellerId, Long orderId);

    /**
     * 배송 상태 업데이트 처리
     * @param sellerId 판매자 ID
     * @param orderId 주문 ID
     * @param dto 배송 상태 갱신 요청 DTO
     */
    void updateDeliveryStatus(Long sellerId, Long orderId, DeliveryStatusUpdateDTO dto);

    //배송 취소 설정
    void cancelOrderDelivery(Long orderId, Long sellerId);
}