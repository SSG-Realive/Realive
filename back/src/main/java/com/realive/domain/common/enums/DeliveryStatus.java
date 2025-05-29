package com.realive.domain.common.enums;


public enum DeliveryStatus {

    DELIVERY_PREPARING("배송준비중"),
    DELIVERY_IN_PROGRESS("배송중"),
    DELIVERY_COMPLETED("배송완료");

    private final String description;

    DeliveryStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
