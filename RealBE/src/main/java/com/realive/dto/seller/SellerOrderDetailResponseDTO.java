package com.realive.dto.seller;

import com.realive.domain.common.enums.DeliveryStatus;
import com.realive.domain.common.enums.DeliveryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerOrderDetailResponseDTO {

    private Long orderId;
    private LocalDateTime orderedAt;
    private String customerName;
    private String customerPhone;
    private String deliveryAddress;
    private String receiverName;
    private int totalPrice;
    private int deliveryFee;
    private String paymentType;

    // 배송 관련 정보
    private DeliveryStatus deliveryStatus;
    private String trackingNumber;
    private String carrier;
    private LocalDateTime startDate;
    private LocalDateTime completeDate;
    private DeliveryType deliveryType;

    // 주문 상품 목록
    private List<OrderItemDetail> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemDetail {
        private Long productId;
        private String productName;
        private int quantity;
        private int price;
        private String imageUrl;
    }
}