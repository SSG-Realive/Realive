package com.realive.dto.logs.stats;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductSalesDetailDTO {
    private Integer productId;
    private String productName; // 상품명 (실제로는 Product 엔티티 등에서 조회)
    private long quantitySold;
    private double totalRevenue;
}
