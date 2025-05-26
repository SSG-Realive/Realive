package com.realive.service.order;

import com.realive.dto.order.DeliveryStatusUpdateDTO;
import com.realive.dto.order.OrderDeliveryResponseDTO;

import java.util.List;

/**
 * 배송 관련 서비스 인터페이스
 */
public interface OrderDeliveryService {

    /**
     * 주문 ID 기반으로 배송 상태를 업데이트함
     *
     * @param orderId 주문 ID
     * @param dto 상태 업데이트 정보 (배송 상태, 운송장 번호, 택배사 등)
     */
    void updateDeliveryStatus(Long sellerId, Long orderId, DeliveryStatusUpdateDTO dto);

    /**
     * 판매자 ID를 기반으로 해당 판매자의 전체 배송 내역을 조회
     *
     * @param sellerId 판매자 ID
     * @return 배송 내역 목록
     */
    List<OrderDeliveryResponseDTO> getDeliveriesBySeller(Long sellerId);
}