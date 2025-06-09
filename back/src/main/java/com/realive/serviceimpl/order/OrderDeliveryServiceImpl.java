package com.realive.serviceimpl.order;

import com.realive.domain.common.enums.DeliveryStatus;
import com.realive.domain.order.Order;
import com.realive.domain.order.OrderDelivery;
import com.realive.domain.order.OrderItem;
import com.realive.domain.product.Product;
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
import java.util.stream.Collectors;

/**
 * 배송 상태 변경 및 조회 서비스 구현체
 */
@Service
@RequiredArgsConstructor
public class OrderDeliveryServiceImpl implements OrderDeliveryService {

    private final SellerOrderDeliveryRepository sellerOrderDeliveryRepository;
    private final OrderDeliveryRepository orderDeliveryRepository;

    @Override
    @Transactional
    public void updateDeliveryStatus(Long sellerId, Long orderId, DeliveryStatusUpdateDTO dto) {
        OrderDelivery delivery = sellerOrderDeliveryRepository
                .findByOrderIdAndSellerId(orderId, sellerId)
                .orElseThrow(() -> new IllegalArgumentException("배송 정보가 존재하지 않습니다."));

        Order order = delivery.getOrder();
        Product product = order.getOrderItems().get(0).getProduct(); // 첫 번째 상품 기준
        if (!product.getSeller().getId().equals(sellerId)) {
            throw new SecurityException("자신의 주문에 대해서만 배송 상태를 변경할 수 있습니다.");
        }

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

        return deliveries.stream().map(delivery -> {
            Order order = delivery.getOrder();
            OrderItem orderItem = order.getOrderItems().get(0);
            Product product = orderItem.getProduct();

            return OrderDeliveryResponseDTO.builder()
                    .orderId(order.getId())
                    .productName(product.getName())
                    .buyerId(order.getCustomer().getId())
                    .deliveryStatus(delivery.getStatus())
                    .startDate(delivery.getStartDate())
                    .completeDate(delivery.getCompleteDate())
                    .trackingNumber(delivery.getTrackingNumber())
                    .carrier(delivery.getCarrier())
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDeliveryResponseDTO getDeliveryByOrderId(Long sellerId, Long orderId) {
        OrderDelivery delivery = sellerOrderDeliveryRepository
                .findByOrderIdAndSellerId(orderId, sellerId)
                .orElseThrow(() -> new IllegalArgumentException("배송 정보가 존재하지 않습니다."));

        Order order = delivery.getOrder();
        OrderItem orderItem = order.getOrderItems().get(0);
        Product product = orderItem.getProduct();

        if (!product.getSeller().getId().equals(sellerId)) {
            throw new SecurityException("자신의 주문이 아닙니다.");
        }

        return OrderDeliveryResponseDTO.builder()
                .orderId(order.getId())
                .productName(product.getName())
                .buyerId(order.getCustomer().getId())
                .deliveryStatus(delivery.getStatus())
                .startDate(delivery.getStartDate())
                .completeDate(delivery.getCompleteDate())
                .trackingNumber(delivery.getTrackingNumber())
                .carrier(delivery.getCarrier())
                .build();
    }
}