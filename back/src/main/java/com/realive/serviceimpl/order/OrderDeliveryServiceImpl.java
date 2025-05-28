package com.realive.serviceimpl.order;

import com.realive.domain.common.enums.SellerDeliveryStatus;
import com.realive.domain.order.Order;
import com.realive.domain.order.SellerOrderDelivery;
import com.realive.domain.product.Product;
import com.realive.dto.order.DeliveryStatusUpdateDTO;
import com.realive.dto.order.OrderDeliveryResponseDTO;
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

    private final SellerOrderDeliveryRepository sellerorderDeliveryRepository;
    private final SellerOrderDeliveryRepository sellerOrderDeliveryRepository;

    /**
     * 배송 상태를 업데이트하고 상태별 처리 시간 자동 기록
     */
    @Override
    @Transactional
    public void updateSellerDeliveryStatus(Long sellerId, Long orderId, DeliveryStatusUpdateDTO dto) {
        SellerOrderDelivery delivery = sellerorderDeliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("배송 정보가 존재하지 않습니다."));

        // 🔒 본인 주문인지 검증
        if (!delivery.getOrder().getProduct().getSeller().getId().equals(sellerId)) {
            throw new SecurityException("자신의 주문에 대해서만 배송 상태를 변경할 수 있습니다.");
        }

        SellerDeliveryStatus currentStatus = delivery.getSellerDeliveryStatus();
        SellerDeliveryStatus newStatus = dto.getDeliveryStatus    ();

        // 🔒 상태 전이 제한 (결제완료 → 배송중 → 배송완료만 허용)
        boolean validTransition =
                (currentStatus == SellerDeliveryStatus.결제완료 && newStatus == SellerDeliveryStatus.배송중) ||
                        (currentStatus == SellerDeliveryStatus.배송중 && newStatus == SellerDeliveryStatus.배송완료);

        if (!validTransition) {
            throw new IllegalStateException("유효하지 않은 배송 상태 전이입니다.");
        }

        // ✅ 운송장 번호와 택배사 정보는 배송중일 때 선택적으로 입력 가능
        if (newStatus == SellerDeliveryStatus.배송중) {
            if (dto.getTrackingNumber() != null) {
                delivery.setTrackingNumber(dto.getTrackingNumber());
            }
            if (dto.getCarrier() != null) {
                delivery.setCarrier(dto.getCarrier());
            }
        }

        // 📦 상태 및 관련 정보 업데이트
        delivery.setSellerDeliveryStatus(newStatus);

        if (newStatus == SellerDeliveryStatus.배송중 && delivery.getStartDate() == null) {
            delivery.setStartDate(LocalDateTime.now());
        }

        if (newStatus == SellerDeliveryStatus.배송완료 && delivery.getCompleteDate() == null) {
            delivery.setCompleteDate(LocalDateTime.now());
        }
    }

    /**
     * 판매자 ID 기준 배송 목록 조회
     */
    @Override
    @Transactional(readOnly = true)
    public List<OrderDeliveryResponseDTO> getDeliveriesBySeller(Long sellerId) {
        List<SellerOrderDelivery> deliveries = sellerOrderDeliveryRepository.findAllBySellerId(sellerId);

        return deliveries.stream().map(delivery -> {
            Order order = delivery.getOrder();
            Product product = order.getProduct();

            return OrderDeliveryResponseDTO.builder()
                    .orderId(order.getId())
                    .productName(product.getName())
                    //.buyerId(order.getCustomer().getId()) // 구매자 ID 포함 필요 시 해제
                    .SellerDeliveryStatus(delivery.getSellerDeliveryStatus())
                    .startDate(delivery.getStartDate())
                    .completeDate(delivery.getCompleteDate())
                    .trackingNumber(delivery.getTrackingNumber())
                    .carrier(delivery.getCarrier())
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public OrderDeliveryResponseDTO getDeliveryByOrderId(Long sellerId, Long orderId) {
        SellerOrderDelivery delivery = sellerOrderDeliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("배송 정보가 존재하지 않습니다"));

        // 본인 주문 검증
        if (!delivery.getOrder().getProduct().getSeller().getId().equals(sellerId)) {
            throw new SecurityException("자신의 상품이 아닌 주문에 접근할 수 없습니다.");

        }

        Order order = delivery.getOrder();
        Product product = order.getProduct();

        return OrderDeliveryResponseDTO.builder()
                .orderId(order.getId())
                .productName(product.getName())
                //.buyerId(order.getCustomer().getId()) // 구매자 ID 포함 필요 시 해제
                .SellerDeliveryStatus(delivery.getSellerDeliveryStatus())
                .startDate(delivery.getStartDate())
                .completeDate(delivery.getCompleteDate())
                .trackingNumber(delivery.getTrackingNumber())
                .carrier(delivery.getCarrier())
                .build();

    }
}