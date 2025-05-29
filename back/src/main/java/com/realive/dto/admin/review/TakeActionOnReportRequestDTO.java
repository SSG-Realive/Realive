package com.realive.dto.admin.review;


import com.realive.domain.common.enums.ReviewReportStatus; // Enum 경로
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TakeActionOnReportRequestDTO { //신고 조치 요청용
    private ReviewReportStatus newStatus; // 변경할 신고의 새로운 상태
    private String adminMemo;             // 관리자 조치 메모 (선택적)
    private Boolean hideReportedReview;   // 신고된 리뷰를 숨김 처리할지 여부 (선택적)
    // private Boolean deleteReportedReview; // 신고된 리뷰를 삭제 처리할지 여부 (선택적)
    // private String reasonForReviewAction; // 리뷰 조치 사유 (선택적)
}
