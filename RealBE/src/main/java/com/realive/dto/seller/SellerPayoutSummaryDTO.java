package com.realive.dto.seller;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SellerPayoutSummaryDTO {
    private final Integer totalPayoutAmount;
    private final Integer totalCommission;
    private final Integer totalSales;
    private final Integer payoutCount;
    // 필요에 따라 필드 추가
}