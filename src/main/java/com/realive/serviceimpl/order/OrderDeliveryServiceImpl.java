package com.realive.serviceimpl.order;

import com.realive.domain.common.enums.DeliveryStatus;
import com.realive.domain.order.OrderDelivery;
import com.realive.domain.order.OrderItem;
import com.realive.dto.order.DeliveryStatusUpdateDTO;
import com.realive.dto.order.OrderDeliveryResponseDTO;
import com.realive.repository.order.OrderItemRepository;
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
    private final OrderItemRepository orderItemRepository;

    @Override
    @Transactional
    public void updateDeliveryStatus(Long sellerId, Long orderId, DeliveryStatusUpdateDTO dto) {
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
        List<OrderDelivery> deliveries = sellerOrderDeliveryRepository.findAllBySellerId(sellerId);

        return deliveries.stream()
                .map(d -> {
                    List<OrderItem> orderItems = orderItemRepository.findByOrderId(d.getOrder().getId());
                    String productName = orderItems.isEmpty() ? "상품 없음" : orderItems.get(0).getProduct().getName();

                    return OrderDeliveryResponseDTO.builder()
                            .orderId(d.getOrder().getId())
                            .productName(productName)
                            .buyerId(d.getOrder().getCustomer().getId())
                            .deliveryStatus(d.getStatus())
                            .startDate(d.getStartDate())
                            .completeDate(d.getCompleteDate())
                            .trackingNumber(d.getTrackingNumber())
                            .carrier(d.getCarrier())
                            .build();
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDeliveryResponseDTO getDeliveryByOrderId(Long sellerId, Long orderId) {
        OrderDelivery delivery = sellerOrderDeliveryRepository
                .findByOrderIdAndSellerId(orderId, sellerId)
                .orElseThrow(() -> new IllegalArgumentException("배송 정보가 존재하지 않습니다."));

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        if (orderItems.isEmpty()) {
            throw new IllegalArgumentException("주문에 상품이 없습니다.");
        }

        if (!orderItems.get(0).getProduct().getSeller().getId().equals(sellerId)) {
            throw new SecurityException("자신의 주문이 아닙니다.");
        }

        String productName = orderItems.get(0).getProduct().getName();

        return OrderDeliveryResponseDTO.builder()
                .orderId(delivery.getOrder().getId())
                .productName(productName)
                .buyerId(delivery.getOrder().getCustomer().getId())
                .deliveryStatus(delivery.getStatus())
                .startDate(delivery.getStartDate())
                .completeDate(delivery.getCompleteDate())
                .trackingNumber(delivery.getTrackingNumber())
                .carrier(delivery.getCarrier())
                .build();
    }
}
