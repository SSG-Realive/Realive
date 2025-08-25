package com.realive.dto.auction;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CustomerProfileDTO {
    private final String receiverName;     // 회원 이름 (혹은 별도 배송지 이름)
    private final String phone;            // 기본 연락처
    private final String deliveryAddress;  // 기본 배송지
}
