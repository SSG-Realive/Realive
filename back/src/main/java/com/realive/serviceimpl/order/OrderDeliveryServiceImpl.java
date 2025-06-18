package com.realive.serviceimpl.order;

import com.realive.domain.common.enums.DeliveryStatus;
import com.realive.domain.order.Order;
import com.realive.domain.order.OrderDelivery;
import com.realive.domain.order.OrderItem;
import com.realive.domain.product.Product;
import com.realive.dto.order.DeliveryStatusUpdateDTO;
import com.realive.dto.order.OrderDeliveryResponseDTO;
import com.realive.repository.order.OrderItemRepository;
import com.realive.repository.order.SellerOrderDeliveryRepository;
import com.realive.repository.product.ProductRepository;
import com.realive.service.order.OrderDeliveryService;
import com.realive.service.seller.SellerPayoutService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderDeliveryServiceImpl implements OrderDeliveryService {

    private final SellerOrderDeliveryRepository sellerOrderDeliveryRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final SellerPayoutService sellerPayoutService;

    @Override
    @Transactional
    public void updateDeliveryStatus(Long sellerId, Long orderId, DeliveryStatusUpdateDTO dto) {
        OrderDelivery delivery = sellerOrderDeliveryRepository
                .findByOrderIdAndSellerId(orderId, sellerId)
                .orElseThrow(() -> new IllegalArgumentException("배송 정보가 존재하지 않습니다."));

        DeliveryStatus currentStatus = delivery.getStatus();
        DeliveryStatus newStatus = dto.getDeliveryStatus();
        Long orderIdForItems = delivery.getOrder().getId();

        log.info("현재 배송 상태 currentStatus={}, 요청된 newStatus={}", currentStatus, newStatus);
        log.info("🔥 DEBUG - 배송 상태 변경 확인: newStatus={}, currentStatus={}", newStatus, currentStatus);
        boolean validTransition = (currentStatus == DeliveryStatus.INIT
                && newStatus == DeliveryStatus.DELIVERY_PREPARING) || // 처음 PREPARING 으로 변경
                (currentStatus == DeliveryStatus.DELIVERY_PREPARING && newStatus == DeliveryStatus.DELIVERY_IN_PROGRESS)
                ||
                (currentStatus == DeliveryStatus.DELIVERY_IN_PROGRESS
                        && newStatus == DeliveryStatus.DELIVERY_COMPLETED)
                ||
                (currentStatus == DeliveryStatus.DELIVERY_IN_PROGRESS
                        && newStatus == DeliveryStatus.DELIVERY_IN_PROGRESS);

        if (!validTransition) {
            throw new IllegalStateException("유효하지 않은 배송 상태 전이입니다.");
        }

        // 상태 업데이트
        delivery.setStatus(newStatus);

        // 배송 준비되면 stock 차감 로직
        if (newStatus == DeliveryStatus.DELIVERY_PREPARING && currentStatus != DeliveryStatus.DELIVERY_PREPARING) {

            List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderIdForItems);

            for (OrderItem item : orderItems) {
                Product product = productRepository.findByIdForUpdate(item.getProduct().getId());

                if (product.getStock() < item.getQuantity()) {
                    throw new IllegalStateException("재고가 부족하여 배송 준비 상태로 변경할 수 없습니다." + product.getName());
                }

                product.setStock(product.getStock() - item.getQuantity());
            }

            // 🚩 INIT → PREPARING 에서만 startDate 찍기
            if (currentStatus == DeliveryStatus.INIT && newStatus == DeliveryStatus.DELIVERY_PREPARING) {
                delivery.setStartDate(LocalDateTime.now());
            }
        }

        // 배송중으로 변경 시 송장번호, 배송사 설정
        if (newStatus == DeliveryStatus.DELIVERY_IN_PROGRESS) {
            if (dto.getTrackingNumber() != null) {
                delivery.setTrackingNumber(dto.getTrackingNumber());
            }
            if (dto.getCarrier() != null) {
                delivery.setCarrier(dto.getCarrier());
            }
        }

        // 배송완료 완료일 설정 + 🚩 isActive 처리 추가
        if (newStatus == DeliveryStatus.DELIVERY_COMPLETED && delivery.getCompleteDate() == null) {
            delivery.setCompleteDate(LocalDateTime.now());
        }

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderIdForItems);

        for (OrderItem item : orderItems) {
            Product product = productRepository.findByIdForUpdate(item.getProduct().getId());

            // 🚩 재고가 0 인 경우에만 isActive = false 처리
            if (product.getStock() == 0 && product.isActive()) {
                product.setActive(false);
                log.info("Product {} 비활성화 처리됨", product.getId());
            }
        }

        if (newStatus == DeliveryStatus.DELIVERY_COMPLETED) {
            log.info("📌 정산 생성 조건문에 진입함");
            try {
                sellerPayoutService.generatePayoutLogIfNotExists(orderIdForItems);
                log.info("🟢 정산 생성 시도 완료 - orderId: {}", orderIdForItems);
            } catch (Exception e) {
                log.warn("❌ 정산 생성 실패 - orderId: {}, 에러: {}", orderIdForItems, e.getMessage());
            }
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

    @Override
    @Transactional
    public void cancelOrderDelivery(Long orderId, Long sellerId) {
        OrderDelivery delivery = sellerOrderDeliveryRepository
                .findByOrderIdAndSellerId(orderId, sellerId)
                .orElseThrow(() -> new IllegalArgumentException("배송 정보가 존재하지 않습니다."));
        
         // 🚩 INIT 상태만 취소 가능
        if (delivery.getStatus() != DeliveryStatus.INIT) {
            throw new IllegalArgumentException("배송 준비 중 상태에서만 취소할 수 있습니다.");
        }
        
        // 🚫 중복 처리 방지
        if (delivery.getStatus() == DeliveryStatus.CANCELLED) {
            throw new IllegalStateException("이미 취소된 주문입니다.");
        }

        // 상태 변경
        delivery.setStatus(DeliveryStatus.CANCELLED);
        log.info("❌ 배송 취소 처리됨 - orderId={}, sellerId={}", orderId, sellerId);

        // 🔒 재고 복원 (동시성 방지를 위해 락 걸고 처리)
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        for (OrderItem item : items) {
            Product product = productRepository.findByIdForUpdate(item.getProduct().getId());

            product.setStock(product.getStock() + item.getQuantity());

            if (!product.isActive()) {
                product.setActive(true); // 재고 생기면 다시 활성화
            }
        }
    }
}
