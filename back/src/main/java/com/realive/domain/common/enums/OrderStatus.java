package com.realive.domain.common.enums;

public enum OrderStatus {

    PAYMENT_COMPLETED("결제완료"),
    PAYMENT_CANCELED("결제취소"),
    PURCHASE_CONFIRMED("구매확정");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}