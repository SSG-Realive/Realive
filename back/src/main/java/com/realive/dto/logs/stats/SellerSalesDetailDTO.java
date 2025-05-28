package com.realive.dto.logs.stats;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SellerSalesDetailDTO {
    private Integer sellerId;
    private String sellerName; // 판매자명 (실제로는 User 엔티티 등에서 조회)
    private long quantitySold;
    private double totalRevenue;
}
