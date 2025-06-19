package com.realive.dto.admin.review; // 사용자님의 현재 패키지 경로

import com.realive.domain.common.enums.ReviewReportStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TakeActionOnReportRequestDTO {

    @NotNull(message = "새로운 신고 처리 상태는 필수입니다.")
    private ReviewReportStatus newStatus;
}
