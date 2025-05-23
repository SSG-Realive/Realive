package com.realive.dto.admin.management;

import java.time.LocalDateTime;

// 리뷰 관리
public class ReviewDTO {
    private Integer id;
    private Integer productId;
    private String productName;
    private Integer customerId;
    private String customerName;
    private String content;
    private Integer rating;
    private String status;
    private LocalDateTime createdAt;
    // 기타 필요한 리뷰 정보
}

