package com.realive.dto.logs.stats;


import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class SalesPeriodStatsDTO {
    private SalesSummaryStatsDTO summary;
    private List<SellerSalesDetailDTO> sellerSalesDetails;   // 판매자별 판매 상세
    private List<DateBasedValueDTO<Double>> dailyRevenueTrend; // 일별 매출 추이
    // 필요시 주별, 월별 매출 추이 DTO 추가
}
