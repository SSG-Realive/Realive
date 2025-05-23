package com.realive.dto.admin.management;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// 상품 관리
public class ProductDTO {
    private Integer id;
    private String name;
    private Integer sellerId;
    private String sellerName;
    private String status;
    private BigDecimal price;
    private Integer inventory;
    private LocalDateTime registeredAt;
    // 기타 필요한 상품 정보
}
