package com.realive.dto.admin.review;

import com.realive.domain.common.enums.ReviewReportStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminReviewReportDetailDTO { //신고 상세 확인용
    // Report 정보
    private Integer reportId;
    private ReviewReportStatus status;
    private String reason; // 전체 신고 사유
    private LocalDateTime reportedAt;
    private LocalDateTime reportUpdatedAt; // 신고 정보 수정일 (BaseTimeEntity)

    // Reporter (신고자) 정보
    private Long reporterId;
    private String reporterName;
    private String reporterEmail;

    // Reported Review (신고된 리뷰) 정보
    private Long reportedReviewId;
     private AdminSellerReviewDetailDTO reportedReviewDetail; // 신고된 리뷰 상세 정보를 여기에 포함할 수도 있음

    // 관리자 조치 내역 (만약 별도 엔티티로 관리한다면)
    // private String adminActionMemo;
    // private LocalDateTime adminActionAt;
    // private String adminUsername;
}
