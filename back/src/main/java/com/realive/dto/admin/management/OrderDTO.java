package com.realive.dto.admin.management;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
// 주문 관리
public class OrderDTO {
    private Integer id;
    private Integer customerId;
    private String customerName;
    private String status;
    private LocalDateTime orderDate;
    private Integer totalAmount;


}

