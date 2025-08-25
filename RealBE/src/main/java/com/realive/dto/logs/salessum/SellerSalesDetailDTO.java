package com.realive.dto.logs.salessum;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SellerSalesDetailDTO {
    private final Integer sellerId;        // 판매자 ID
    private final String sellerName;       // 판매자 이름
    private final Long salesCount;         // 판매 건수
    private final Double totalRevenue;     // 총 매출
}
