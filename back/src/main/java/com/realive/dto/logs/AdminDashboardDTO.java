package com.realive.dto.logs; // 또는 com.realive.dto.admin 등 적절한 패키지

import lombok.AllArgsConstructor; // 모든 필드를 받는 생성자 (Builder가 사용)
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor; // 기본 생성자 (선택 사항, JSON 역직렬화 등 필요시)

import java.util.List;

// 통합 컨테이너 DTO
@Getter
@Builder
@NoArgsConstructor  // 필요에 따라 추가
@AllArgsConstructor // Builder 사용 시 모든 필드를 받는 생성자가 필요합니다.
// final 필드가 아니게 되면 Lombok이 자동으로 생성해주기도 합니다.
public class AdminDashboardDTO {

    // --- 기존 필드들 ---
    // final 키워드를 제거해야 새로운 필드를 추가하고 유연하게 사용할 수 있습니다.
    // 만약 불변성을 유지하고 싶다면, 모든 필드를 포함하는 생성자를 통해 값을 설정해야 합니다.
    private ProductLogDTO productLog;
    private List<PenaltyLogDTO> penaltyLogs;

    // --- 새롭게 추가될 필드 ---
    private int pendingSellerCount; // 승인 대기 중인 판매자 수
}
