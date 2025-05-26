package com.realive.domain.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReturnReason {
    // 고객 단순 변심 (고객 귀책)
    CHANGING_MIND("단순 변심"),
    INCORRECT_ORDER("주문 착오"),

    // 상품 자체의 문제 (판매자/상품 귀책)
    PRODUCT_DAMAGE("상품 파손/손상"),
    DIFFERENT_FROM_DESCRIPTION("설명과 다른 상품"),
    MISSING_PARTS("구성품 누락"),
    FUNCTIONAL_PROBLEM("기능상 문제 발생"),

    // 배송 문제 (판매자/배송사 귀책)
    INCORRECT_DELIVERY("오배송"),

    // 기타 (경우에 따라 귀책 여부 판단 필요)
    OTHER_REASON("기타 (사유 직접 입력)");

    private final String description;

    public static ReturnReason fromDescription(String description) {
        for (ReturnReason reason : ReturnReason.values()) {
            if (reason.getDescription().equals(description)) {
                return reason;
            }
        }
        throw new IllegalArgumentException("Invalid ReturnReason description: " + description);
    }

    /**
     * 해당 반품 사유가 고객의 귀책 사유인지 여부를 반환합니다.
     * 단순 변심, 주문 착오 등은 고객 귀책 사유로 간주됩니다.
     * @return 고객 귀책 사유인 경우 true, 그렇지 않으면 false
     */
    public boolean isCustomerFault() {
        return this == CHANGING_MIND || this == INCORRECT_ORDER;
    }
}