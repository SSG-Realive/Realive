package com.realive.domain.common.enums;

public enum PaymentType {

    CARD("신용/체크카드"),
    CELL_PHONE("휴대폰"),
    ACCOUNT("계좌입금/무통장입금");

    private final String description;

    PaymentType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
