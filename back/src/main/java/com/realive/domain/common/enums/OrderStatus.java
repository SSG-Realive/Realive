package com.realive.domain.common.enums;

import lombok.Getter;

@Getter
public enum OrderStatus {

    // 초기 주문 및 결제 관련 상태
    ORDER_RECEIVED("주문접수"),
    PAYMENT_COMPLETED("결제완료"),

    // 취소 및 환불 관련 상태
    PAYMENT_CANCELED("결제취소"),
    PURCHASE_CANCELED("구매취소"),
    REFUND_COMPLETED("환불완료"),

    // 최종 구매 확정 상태
    PURCHASE_CONFIRMED("구매확정");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

}