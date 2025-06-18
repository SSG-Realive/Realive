package com.realive.service.seller;

import com.realive.dto.seller.SellerDashboardResponseDTO;

public interface SellerDashboardService {

    /**
     * 판매자 대시보드용 데이터 조회
     * @param sellerId 판매자 ID
     * @return SellerDashboardResponseDTO
     */
    SellerDashboardResponseDTO getDashboardInfo(Long sellerId);
}