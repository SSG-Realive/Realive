package com.realive.dto.auction;

import lombok.*;
import java.time.LocalDateTime;

// 경매 목록 조회
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuctionListResponseDTO {

    private Integer id;
    private Integer productId;
    private Integer startPrice;
    private Integer currentPrice;
    private LocalDateTime endTime;
    private boolean isClosed;
}
