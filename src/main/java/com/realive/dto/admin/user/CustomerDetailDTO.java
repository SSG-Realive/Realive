package com.realive.dto.admin.user;


import com.realive.domain.customer.Gender;
import com.realive.domain.customer.SignupMethod;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
// import java.util.List; // 예: 최근 주문 요약 List<OrderSummaryDTO> 등

@Getter
@Builder
public class CustomerDetailDTO {
    // Customer 엔티티 필드 기반
    private Long id;
    private String email;
    private String name;
    private String phone;
    private String address;
    private Boolean isVerified;
    private Boolean isActive;
    private Integer penaltyScore;
    private LocalDate birth;
    private Gender gender;
    private SignupMethod signupMethod;
    private LocalDateTime createdAt; // Customer 엔티티의 'created' 필드
    private LocalDateTime updatedAt; // Customer 엔티티의 'updated' 필드

    // 추가적으로 보여줄 수 있는 연관 정보 (선택적)
    // private Long totalOrderCount;
    // private BigDecimal totalOrderAmount;
    // private List<RecentActivityDTO> recentActivities;
}
