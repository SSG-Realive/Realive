package com.realive.dto.order;

import com.realive.domain.common.enums.DeliveryStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OrderDeliveryResponseDTO {

    private Long orderId;                       // 주문 ID
    private String productName;                 // 주문된 상품 이름
    private Long buyerId;                       // 구매자 ID
    private DeliveryStatus deliveryStatus;      // 현재 배송 상태 (결제완료, 배송중, 배송완료)
    private LocalDateTime startDate;            // 배송 시작일 (배송 중 상태일 때 기록)
    private LocalDateTime completeDate;         // 배송 완료일 (배송 완료 상태일 때 기록)
    private String trackingNumber;              // 운송장 번호
    private String carrier;                     // 택배사 이름
}