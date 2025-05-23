package com.realive.dto.admin.management;

import jdk.jshell.Snippet;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// 상품 관리
@Builder
public class ProductDTO {
    private Integer id;
    private String name;
    private Integer sellerId;
    private String sellerName;
    private String status;
    private BigDecimal price;
    private Integer inventory;
    private LocalDateTime registeredAt;


}
