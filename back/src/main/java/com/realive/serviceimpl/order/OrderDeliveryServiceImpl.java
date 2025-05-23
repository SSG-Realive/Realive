package com.realive.serviceimpl.order;

import com.realive.domain.common.enums.DeliveryStatus;
import com.realive.domain.order.Order;
import com.realive.domain.order.OrderDelivery;
import com.realive.domain.product.Product;
import com.realive.dto.order.DeliveryStatusUpdateDTO;
import com.realive.dto.order.OrderDeliveryResponseDTO;
import com.realive.repository.order.OrderDeliveryRepository;
import com.realive.service.order.OrderDeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 배송 상태 변경 및 조회 서비스 구현체
 */
@Service
@RequiredArgsConstructor
public class OrderDeliveryServiceImpl implements OrderDeliveryService {

    private final OrderDeliveryRepository orderDeliveryRepository;

    /**
     * 배송 상태를 업데이트하고 상태별 처리 시간 자동 기록
     */
    @Override
    @Transactional
    public void updateDeliveryStatus(Long sellerId, Long orderId, DeliveryStatusUpdateDTO dto) {
        OrderDelivery delivery = orderDeliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("배송 정보가 존재하지 않습니다."));

        if (!delivery.getOrder().getProduct().getSeller().getId().equals(sellerId)) {
            throw new SecurityException("자신의 주문에 대해서만 배송 상태를 변경할 수 있습니다.");
        }

        DeliveryStatus currentStatus = delivery.getDeliveryStatus(); // 🔹 추가
        DeliveryStatus newStatus = dto.getDeliveryStatus();

        // 🔒 상태 전이 제한 로직 추가
        if (!((currentStatus == DeliveryStatus.결제완료 && newStatus == DeliveryStatus.배송중) ||
                (currentStatus == DeliveryStatus.배송중 && newStatus == DeliveryStatus.배송완료))) {
            throw new IllegalStateException("유효하지 않은 배송 상태 전이입니다.");
        }

        if (newStatus == DeliveryStatus.배송중 &&
                (dto.getTrackingNumber() == null || dto.getCarrier() == null)) {
            throw new IllegalArgumentException("배송중 상태에서는 운송장 번호와 택배사 정보가 필수입니다.");
        }

        delivery.setDeliveryStatus(newStatus);
        delivery.setTrackingNumber(dto.getTrackingNumber());
        delivery.setCarrier(dto.getCarrier());

        if (newStatus == DeliveryStatus.배송중 && delivery.getStartDate() == null) {
            delivery.setStartDate(LocalDateTime.now());
        }

        if (newStatus == DeliveryStatus.배송완료 && delivery.getCompleteDate() == null) {
            delivery.setCompleteDate(LocalDateTime.now());
        }
    }

    /**
     * 판매자 ID 기준 배송 목록 조회
     */
    @Override
    @Transactional(readOnly = true)
    public List<OrderDeliveryResponseDTO> getDeliveriesBySeller(Long sellerId) {
        List<OrderDelivery> deliveries = orderDeliveryRepository.findAllBySellerId(sellerId);

        return deliveries.stream().map(delivery -> {
            Order order = delivery.getOrder();
            Product product = order.getProduct();

            return OrderDeliveryResponseDTO.builder()
                    .orderId(order.getId())
                    .productName(product.getName())
                    //.buyerId(order.getCustomer().getId()) // 구매자 ID 포함 필요 시 해제
                    .deliveryStatus(delivery.getDeliveryStatus())
                    .startDate(delivery.getStartDate())
                    .completeDate(delivery.getCompleteDate())
                    .trackingNumber(delivery.getTrackingNumber())
                    .carrier(delivery.getCarrier())
                    .build();
        }).collect(Collectors.toList());
    }
}
