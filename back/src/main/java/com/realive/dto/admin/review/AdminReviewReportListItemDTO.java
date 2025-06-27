package com.realive.dto.admin.review;

import com.realive.domain.common.enums.ReviewReportStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminReviewReportListItemDTO {
    private Integer reportId;                   // ReviewReport.id (Integer)
    private ReviewReportStatus status;          // 신고 처리 상태
    private Long reportedReviewId;              // 신고된 SellerReview.id (Long)
    private Long reporterId;                    // 신고자 Customer.id (Long)
    private String reporterName;                // 신고자 이름
    private String reason;               // 신고 사유
    private LocalDateTime reportedAt;           // 신고일 (ReviewReport.createdAt)
}
