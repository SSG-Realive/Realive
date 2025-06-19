package com.realive.dto.logs.stats;


import lombok.Builder;
import lombok.Getter;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class ReviewPeriodStatsDTO {
    private ReviewSummaryStatsDTO summary;
    private Map<Integer, Long> ratingDistribution; // 평점 분포 (별점: 개수) (요구사항: 평점 분포)
    private List<DateBasedValueDTO<Long>> dailyReviewCountTrend; // 일별 리뷰 작성 수 추이
    // 필요시 주별, 월별 리뷰 작성 수 추이 DTO 추가
}
