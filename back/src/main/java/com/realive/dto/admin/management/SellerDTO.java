package com.realive.dto.admin.management;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// 판매자 관리
public class SellerDTO {
    private Integer id;
    private String name;
    private String status;
    private LocalDateTime registeredAt;
    private Integer productCount;
    private Integer totalSales;
    private BigDecimal commission;
    // 기타 필요한 판매자 정보
}
