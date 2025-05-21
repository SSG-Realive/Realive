package com.realive.dto.order;

import com.realive.domain.common.enums.DeliveryStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OrderDeliveryResponseDTO {
    // 주문 ID
    private Long orderId;

    // 주문된 상품 이름
    private String productName;

    // 구매자 ID
    private Long buyerId;

    // 현재 배송 상태 (PAYMENT, SHIPPING, DELIVERED)
    private DeliveryStatus deliveryStatus;

    // 배송 시작일 (배송 중 상태일 때 기록)
    private LocalDateTime startDate;

    // 배송 완료일 (배송 완료 상태일 때 기록)
    private LocalDateTime completeDate;

    // 운송장 번호
    private String trackingNumber;

    // 택배사 이름
    private String carrier;
}