package com.realive.dto.order;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderStatisticsDTO {
    private long totalOrders;      // 전체 주문 수
    private long preparingOrders;  // 배송 준비 중
    private long inProgressOrders; // 배송 중
    private long completedOrders;  // 배송 완료
}