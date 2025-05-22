package com.realive.service.admin.logs;

import com.realive.dto.logs.salessum.DailySalesSummaryDTO;
import com.realive.dto.logs.salessum.MonthlySalesLogDetailListDTO;
import com.realive.dto.logs.salessum.MonthlySalesSummaryDTO;
import com.realive.dto.logs.salessum.SalesLogDetailListDTO;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

public interface StatService {
    // 일별 통계
    DailySalesSummaryDTO getDailySalesSummary(LocalDate date);
    SalesLogDetailListDTO getDailySalesLogDetails(LocalDate date);

    // 월별 통계
    MonthlySalesSummaryDTO getMonthlySalesSummary(YearMonth yearMonth);
    MonthlySalesLogDetailListDTO getMonthlySalesLogDetails(YearMonth yearMonth);
    List<DailySalesSummaryDTO> getDailySummariesInMonth(YearMonth yearMonth);

    // 판매자별 통계
    DailySalesSummaryDTO getSellerDailySalesSummary(Integer sellerId, LocalDate date);
    MonthlySalesSummaryDTO getSellerMonthlySalesSummary(Integer sellerId, YearMonth yearMonth);

    // 상품별 통계
    DailySalesSummaryDTO getProductDailySalesSummary(Integer productId, LocalDate date);
    MonthlySalesSummaryDTO getProductMonthlySalesSummary(Integer productId, YearMonth yearMonth);

    // 대시보드용 통합 통계
    Map<String, Object> getDashboardStats(LocalDate date);
}