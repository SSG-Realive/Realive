package com.realive.domain.common.enums;


public enum ReviewReportStatus {
    PENDING,        // 접수됨 (처리 대기) - 기본값으로 사용 가능
    UNDER_REVIEW,   // 검토 중 (관리자가 확인했으나 아직 조치 전)
    RESOLVED_KEPT,  // 처리 완료 - 리뷰 유지 (신고 내용 문제 없음)
    RESOLVED_HIDDEN, // 처리 완료 - 리뷰 숨김/삭제 (신고 내용 타당)
    RESOLVED_REJECTED // 처리 완료 - 신고 기각 (신고 자체가 부적절)

}
