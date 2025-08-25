package com.realive.dto.admin.user;


import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
// import java.util.List; // 예: 최근 상품 목록 List<ProductSummaryDTO> 등

@Getter
@Builder
public class SellerDetailDTO {
    // Seller 엔티티 필드 기반
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String businessNumber;
    private Boolean isApproved;
    private LocalDateTime approvedAt;
    private Boolean isActive;
    private LocalDateTime createdAt; // BaseTimeEntity 상속
    private LocalDateTime updatedAt; // BaseTimeEntity 상속

    // 추가적으로 보여줄 수 있는 연관 정보 (선택적)
    // private Long totalProductCount;
    // private Long totalSalesCount;
    // private BigDecimal totalSalesAmount;
}
