package com.realive.dto.bid;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

// 입찰 취소 요청 DTO
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BidCancelRequestDTO {
    @NotNull(message = "입찰 ID는 필수입니다.")
    private Integer bidId;

    @NotNull(message = "고객 ID는 필수입니다.")
    private Integer customerId;

    @Size(length = 200)
    private String cancelReason; // 취소 사유 (선택 사항)
}