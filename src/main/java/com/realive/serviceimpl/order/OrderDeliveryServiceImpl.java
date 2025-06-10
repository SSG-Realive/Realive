package com.realive.serviceimpl.order;

import com.realive.domain.common.enums.DeliveryStatus;
import com.realive.domain.order.OrderDelivery;
import com.realive.dto.order.DeliveryStatusUpdateDTO;
import com.realive.dto.order.OrderDeliveryResponseDTO;
import com.realive.repository.order.OrderDeliveryRepository;
import com.realive.repository.order.SellerOrderDeliveryRepository;
import com.realive.service.order.OrderDeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderDeliveryServiceImpl implements OrderDeliveryService {

    private final SellerOrderDeliveryRepository sellerOrderDeliveryRepository;
    private final OrderDeliveryRepository orderDeliveryRepository;

    @Override
    @Transactional
    public void updateDeliveryStatus(Long sellerId, Long orderId, DeliveryStatusUpdateDTO dto) {
        // 여기는 상태 변경이므로 엔티티 필요 → 기존 findByOrderIdAndSellerId (OrderDelivery 반환)도 유지 필요
        OrderDelivery delivery = sellerOrderDeliveryRepository
                .findByOrderIdAndSellerId(orderId, sellerId)
                .orElseThrow(() -> new IllegalArgumentException("배송 정보가 존재하지 않습니다."));

        DeliveryStatus currentStatus = delivery.getStatus();
        DeliveryStatus newStatus = dto.getDeliveryStatus();

        boolean validTransition =
                (currentStatus == DeliveryStatus.DELIVERY_PREPARING && newStatus == DeliveryStatus.DELIVERY_IN_PROGRESS) ||
                (currentStatus == DeliveryStatus.DELIVERY_IN_PROGRESS && newStatus == DeliveryStatus.DELIVERY_COMPLETED);

        if (!validTransition) {
            throw new IllegalStateException("유효하지 않은 배송 상태 전이입니다.");
        }

        if (newStatus == DeliveryStatus.DELIVERY_IN_PROGRESS) {
            if (dto.getTrackingNumber() != null) {
                delivery.setTrackingNumber(dto.getTrackingNumber());
            }
            if (dto.getCarrier() != null) {
                delivery.setCarrier(dto.getCarrier());
            }
        }

        delivery.setStatus(newStatus);

        if (newStatus == DeliveryStatus.DELIVERY_IN_PROGRESS && delivery.getStartDate() == null) {
            delivery.setStartDate(LocalDateTime.now());
        }

        if (newStatus == DeliveryStatus.DELIVERY_COMPLETED && delivery.getCompleteDate() == null) {
            delivery.setCompleteDate(LocalDateTime.now());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDeliveryResponseDTO> getDeliveriesBySeller(Long sellerId) {
        // 👉 DTO projection 바로 사용
        return sellerOrderDeliveryRepository.findAllDeliveryDTOBySellerId(sellerId);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDeliveryResponseDTO getDeliveryByOrderId(Long sellerId, Long orderId) {
        // 👉 DTO projection 바로 사용
        return sellerOrderDeliveryRepository
                .findDeliveryDTOByOrderIdAndSellerId(orderId, sellerId)
                .orElseThrow(() -> new IllegalArgumentException("배송 정보가 존재하지 않습니다."));
    }
}
