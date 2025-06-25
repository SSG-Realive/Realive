package com.realive.dto.seller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerSalesStatsDTO {
    private long totalOrders;
    private double totalRevenue;
    private double totalFees;
    private List<DailySalesDTO> dailySalesTrend;
    private List<MonthlySalesDTO> monthlySalesTrend;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailySalesDTO {
        private LocalDate date;
        private long orderCount;
        private double revenue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlySalesDTO {
        private String yearMonth;
        private long orderCount;
        private double revenue;
    }
}