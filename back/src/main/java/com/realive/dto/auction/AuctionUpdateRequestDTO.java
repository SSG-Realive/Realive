package com.realive.dto.auction;

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

    @Future(message = "종료 시간은 현재 시점 이후여야 합니다.")
    private LocalDateTime endTime;

    private Boolean closed;
}
