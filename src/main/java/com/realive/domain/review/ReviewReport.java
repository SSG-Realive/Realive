package com.realive.domain.review;


import com.realive.domain.common.BaseTimeEntity;
import com.realive.domain.common.enums.ReviewReportStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "review_reports")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 엔티티는 기본 생성자가 필요
@AllArgsConstructor // @Builder는 이 생성자를 사용함
@Builder // Builder 패턴 적용
public class ReviewReport extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "seller_review_id")
    private Integer sellerReviewId; // SellerReview.id 참조 (Long으로 변경 고려)

    @Column(name = "reporter_id")
    private Integer reporterId; // Customer.id 참조 (Long으로 변경 고려)

    @Column(length = 1000)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default // Builder 사용 시 기본값 설정
    private ReviewReportStatus status = ReviewReportStatus.PENDING; // 기본 상태는 PENDING

    // 상태 업데이트를 위한 메소드 (Setter 대신 사용 권장)
    public void updateStatus(ReviewReportStatus newStatus) {
        this.status = newStatus;
    }
}
