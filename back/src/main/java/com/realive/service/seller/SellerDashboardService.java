package com.realive.service.seller;

import com.realive.dto.seller.SellerDashboardResponseDTO;
import com.realive.dto.seller.SellerSalesStatsDTO;

import java.time.LocalDate;
import java.util.List;

public interface SellerDashboardService {

    /**
     * 판매자 대시보드용 데이터 조회
     * @param sellerId 판매자 ID
     * @return SellerDashboardResponseDTO
     */
    SellerDashboardResponseDTO getDashboardInfo(Long sellerId);

    // 새로 추가되는 메서드들
    SellerSalesStatsDTO getSalesStatistics(Long sellerId, LocalDate startDate, LocalDate endDate);
    List<SellerSalesStatsDTO.DailySalesDTO> getDailySalesTrend(Long sellerId, LocalDate startDate, LocalDate endDate);
    List<SellerSalesStatsDTO.MonthlySalesDTO> getMonthlySalesTrend(Long sellerId, LocalDate startDate, LocalDate endDate);
}