package com.realive.domain.common.enums;

public enum SellerApprovalStatusByAdmin {
    PENDING_REVIEW("검토 대기중"),
    APPROVED("승인 완료"),
    REJECTED("승인 거절"),
    ON_HOLD("승인 보류");

    private final String displayName;

    SellerApprovalStatusByAdmin(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }  //다른 클래스에서도 사용 할 수 있게
}
