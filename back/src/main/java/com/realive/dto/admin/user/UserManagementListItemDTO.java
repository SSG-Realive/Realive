package com.realive.dto.admin.user;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class UserManagementListItemDTO {
    private Long id;
    private String userType;        // "CUSTOMER" 또는 "SELLER"
    private String name;
    private String email;
    private String phone;           // 선택적
    private Boolean isActive;
    private LocalDateTime createdAt;  // Customer.getCreatedAt() 또는 Seller.getCreatedAt() 사용 가능
    private Boolean isApproved;     // Seller인 경우에만 해당
}
