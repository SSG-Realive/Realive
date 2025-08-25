package com.realive.domain.common.enums;

public enum PaymentType {

    CARD("카드"),
    CELL_PHONE("휴대폰"),
    ACCOUNT("가상계좌");

    private final String description;

    PaymentType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
