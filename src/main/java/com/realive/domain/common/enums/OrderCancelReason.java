package com.realive.domain.common.enums;

public enum OrderCancelReason {

    INCORRECT_DELIVERY_ADDRESS("배송지를 잘못 입력함"),
    CHANGING_MIND("상품이 마음에 들지 않음(단순변심)"),
    REPURCHASE_AFTER_ADD_ANOTHER_ITEM("다른 물품 추가 후 재구매"),
    THE_OTHER_REASON("기타");

    private final String description;

    OrderCancelReason(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}


