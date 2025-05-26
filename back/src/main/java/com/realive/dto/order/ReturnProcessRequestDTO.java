package com.realive.dto.order;

import com.realive.domain.common.enums.ReturnStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReturnProcessRequestDTO {
    @NotNull(message = "반품 요청 ID는 필수입니다.")
    private Long returnRequestId;

    @NotNull(message = "변경할 반품 상태는 필수입니다.")
    private ReturnStatus newStatus;

    private String adminMemo; // 관리자 메모

    @Min(value = 0, message = "환불 금액은 0 이상이어야 합니다.")
    private Integer refundAmount; // 환불 승인 시 금액 (선택 사항)
}