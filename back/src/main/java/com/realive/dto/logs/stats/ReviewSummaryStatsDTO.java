package com.realive.dto.logs.stats;


import lombok.Builder;
import lombok.Getter;
// import java.util.Map; // 평점 분포를 여기에 넣을 수도 있음

@Getter
@Builder
public class ReviewSummaryStatsDTO {
    private long totalReviewsInPeriod;        // 기간 내 총 리뷰 수 (요구사항: 작성된 리뷰 수)
    private long newReviewsInPeriod;          // 기간 내 신규 작성 리뷰 수
    private double averageRatingInPeriod;     // 기간 내 평균 평점 (요구사항: 평균 평점)
    private double deletionRate;              // 기간 내 삭제 비율 (요구사항: 삭제 리뷰 비율)
}
