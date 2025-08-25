package com.realive.dto.auction;

import com.realive.domain.common.enums.AuctionStatus;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

// 경매 수정
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuctionUpdateRequestDTO {

    @NotNull(message = "경매 ID는 필수입니다.")
    private Integer id;

    // 시작시간은 수정 시 현재 시점 이후 제약 제거 (이미 시작된 경매도 수정 가능하도록)
    private LocalDateTime startTime;

    @Future(message = "종료 시간은 현재 시점 이후여야 합니다.")
    private LocalDateTime endTime;

    private AuctionStatus status;
}
