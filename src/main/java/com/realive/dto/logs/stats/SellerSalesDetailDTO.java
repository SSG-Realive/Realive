package com.realive.dto.logs.stats;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SellerSalesDetailDTO {
    private Integer sellerId;
    private String sellerName; // 판매자명 (실제로는 User 엔티티 등에서 조회)
    private long salesCount; // 판매자별 총 판매 건수 (또는 낙찰 건수)
    private double totalRevenue;
}
