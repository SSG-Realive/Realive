package com.realive.dto.admin.management;

import java.time.LocalDateTime;

// 주문 관리
public class OrderDTO {
    private Integer id;
    private Integer customerId;
    private String customerName;
    private String status;
    private LocalDateTime orderDate;
    private Integer totalAmount;

    public class Builder {
    }
//    private List<OrderItemDTO> items;
    // 기타 필요한 주문 정보
}

