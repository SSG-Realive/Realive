package com.realive.dto.order;

import com.realive.domain.common.enums.DeliveryStatus;
import lombok.Getter;
import lombok.Setter;

/**
 * 배송 상태 변경 요청 DTO
 * 판매자가 배송 상태를 변경할 때 사용하는 요청 객체
 */
@Getter
@Setter
public class DeliveryStatusUpdateDTO {

    private DeliveryStatus deliveryStatus;  // 결제완료, 배송중, 배송완료
    private String trackingNumber;          // 운송장 번호 (운송장 번호는 배송중일 때 필수)
    private String carrier;                 // 택배사 (택배사 이름은 배송중일 때 필수)
}