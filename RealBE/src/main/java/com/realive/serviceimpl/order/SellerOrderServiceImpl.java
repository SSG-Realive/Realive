package com.realive.serviceimpl.order;

import com.realive.domain.common.enums.MediaType;
import com.realive.domain.common.enums.DeliveryStatus;
import com.realive.domain.order.Order;
import com.realive.domain.order.OrderDelivery;
import com.realive.domain.order.OrderItem;
import com.realive.dto.order.OrderStatisticsDTO;
import com.realive.dto.seller.SellerOrderDetailResponseDTO;
import com.realive.repository.order.OrderDeliveryRepository;
import com.realive.repository.order.OrderItemRepository;
import com.realive.repository.order.OrderRepository;
import com.realive.repository.product.ProductImageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.realive.dto.order.SellerOrderListDTO;
import com.realive.dto.order.SellerOrderSearchCondition;
import com.realive.repository.order.SellerOrderDeliveryRepository;
import com.realive.service.order.SellerOrderService;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SellerOrderServiceImpl implements SellerOrderService {

    private final SellerOrderDeliveryRepository sellerOrderDeliveryRepository;
    private final OrderRepository orderRepository; // ✅ 추가 필요
    private final OrderItemRepository orderItemRepository; // ✅ 추가 필요
    private final ProductImageRepository productImageRepository; // ✅ 추가 필요
    private final OrderDeliveryRepository orderDeliveryRepository; // ✅ 이 줄 추가

    @Override
    public Page<SellerOrderListDTO> getOrderListBySeller(Long sellerId, SellerOrderSearchCondition condition,
            Pageable pageable) {
        
        return sellerOrderDeliveryRepository.getOrderListBySeller(sellerId, condition, pageable);

    }

    // ✅ 추가할 메서드: 주문 상세 조회 구현
    @Override
    public SellerOrderDetailResponseDTO getOrderDetail(Long sellerId, Long orderId) {
        // 1. 판매자 권한 확인 및 주문 조회
        Order order = orderRepository.findBySellerIdAndOrderId(sellerId, orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없거나 권한이 없습니다."));

        // 2. 주문 상품 목록 조회
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);

        // 3. 상품 이미지 URL 조회
        List<Long> productIds = orderItems.stream()
                .map(item -> item.getProduct().getId())
                .collect(Collectors.toList());

        Map<Long, String> thumbnailUrls = productImageRepository.findThumbnailUrlsByProductIds(productIds, MediaType.IMAGE)
                .stream()
                .collect(Collectors.toMap(
                        arr -> (Long) arr[0],
                        arr -> (String) arr[1]
                ));

        // 4. OrderItemDetail 리스트 생성
        List<SellerOrderDetailResponseDTO.OrderItemDetail> itemDetails = orderItems.stream()
                .map(item -> SellerOrderDetailResponseDTO.OrderItemDetail.builder()
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .imageUrl(thumbnailUrls.get(item.getProduct().getId()))
                        .build())
                .collect(Collectors.toList());

        // 5. 배송 정보 조회
        Optional<OrderDelivery> orderDelivery = orderDeliveryRepository.findByOrder(order);

        // 6. DTO 생성 및 반환
        return SellerOrderDetailResponseDTO.builder()
                .orderId(order.getId())
                .orderedAt(order.getOrderedAt())
                .customerName(order.getCustomer().getName())
                .customerPhone(order.getCustomer().getPhone())
                .deliveryAddress(order.getDeliveryAddress())
                .receiverName(order.getCustomer().getName()) // receiverName이 없으면 고객 이름 사용
                .totalPrice(order.getTotalPrice())
                .deliveryFee(calculateDeliveryFee(orderItems))
                .paymentType(order.getPaymentMethod())
                .deliveryStatus(orderDelivery.map(OrderDelivery::getStatus).orElse(DeliveryStatus.INIT))
                .trackingNumber(orderDelivery.map(OrderDelivery::getTrackingNumber).orElse(null))
                .carrier(orderDelivery.map(OrderDelivery::getCarrier).orElse(null))
                .startDate(orderDelivery.map(OrderDelivery::getStartDate).orElse(null))
                .completeDate(orderDelivery.map(OrderDelivery::getCompleteDate).orElse(null))
                .deliveryType(null) // deliveryType이 없으면 null
                .items(itemDetails)
                .build();
    }

    // ✅ 배송비 계산 메서드 추가
    private int calculateDeliveryFee(List<OrderItem> orderItems) {
        return orderItems.size() * 3000; // 상품당 3000원 배송비
    }


    @Override
    public OrderStatisticsDTO getOrderStatistics(Long sellerId) {
        return sellerOrderDeliveryRepository.getOrderStatisticsBySeller(sellerId);
    }

}
