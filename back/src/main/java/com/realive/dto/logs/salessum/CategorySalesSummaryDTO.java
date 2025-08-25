package com.realive.dto.logs.salessum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CategorySalesSummaryDTO {
    private Long categoryId;
    private String categoryName;
    private Long totalSalesCount;    // COUNT(sl.id)는 Long을 반환
    private Long totalSalesAmount;   // SUM(sl.totalPrice)는 Long을 반환 (Integer의 합이므로)
    private Integer totalProfitAmount; // 이익금 계산 로직 (일단 Integer로 가정)
}
