package com.realive.domain.common.enums;


public enum DeliveryStatus {

    INIT("판매자대기"),  // ← 추가
    DELIVERY_PREPARING("배송준비중"),
    DELIVERY_IN_PROGRESS("배송중"),
    DELIVERY_COMPLETED("배송완료"),
    CANCELLED("배송 취소");             // ✅ 판매자에 의한 배송 전 취소

    private final String description;

    DeliveryStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
