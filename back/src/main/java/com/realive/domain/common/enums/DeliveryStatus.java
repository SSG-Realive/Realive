package com.realive.domain.common.enums;

/**
 * 배송 상태
 * 결제 완료 → 배송 중 → 배송 완료
 */
public enum DeliveryStatus {
    PAYMENT,    // 결제 완료
    SHIPPING,   // 배송 중
    DELIVERED   // 배송 완료
}