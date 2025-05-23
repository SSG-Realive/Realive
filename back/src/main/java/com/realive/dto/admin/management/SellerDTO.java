package com.realive.dto.admin.management;

import jdk.jshell.Snippet;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// 판매자 관리
@Builder
public class SellerDTO {
    private Integer id;
    private String name;
    private String status;
    private LocalDateTime registeredAt;
    private Integer productCount;
    private Integer totalSales;
    private BigDecimal commission;


}
