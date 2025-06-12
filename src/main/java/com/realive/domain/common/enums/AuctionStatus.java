package com.realive.domain.common.enums;

public enum AuctionStatus {
    PROCEEDING,    // 진행중
    COMPLETED,     // 정상 종료(낙찰/유찰 포함)
    CANCELLED,     // 관리자/시스템에 의한 취소
    FAILED         // 기타 실패 상태(유찰 등, 필요 시)
}

