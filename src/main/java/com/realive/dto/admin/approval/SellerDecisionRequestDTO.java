package com.realive.dto.admin.approval; // DTO 패키지 경로 예시

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 요청 본문을 받기 위한 DTO이므로, 일반적으로 @Setter도 필요합니다.
// 유효성 검증을 위해 @NotNull, @Min 등의 어노테이션을 추가할 수 있습니다.
@Getter
@Setter // JSON 요청 본문을 객체로 매핑하기 위해 Setter 또는 모든 필드를 받는 생성자 필요
@NoArgsConstructor
public class SellerDecisionRequestDTO {
    private Long sellerId;    // 처리할 판매자 ID
    private boolean approve;  // true: 승인, false: 거부
}
