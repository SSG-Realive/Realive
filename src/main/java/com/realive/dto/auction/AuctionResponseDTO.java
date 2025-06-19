package com.realive.dto.auction;

import com.realive.domain.auction.Auction;
import com.realive.domain.common.enums.AuctionStatus;
import lombok.*;
import java.time.LocalDateTime;

// 상세 경매 조회 응답
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuctionResponseDTO {

    private Integer id;
    private Integer startPrice;
    private Integer currentPrice;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private AuctionStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private AdminProductDTO adminProduct;

    public static AuctionResponseDTO fromEntity(Auction auction, AdminProductDTO productDTO) {
        if (auction == null) {
            throw new IllegalArgumentException("경매 정보는 null일 수 없습니다.");
        }

        // endTime이 지났고, 아직 PROCEEDING이면 동적으로 COMPLETED로 반환
        AuctionStatus dynamicStatus = auction.getStatus();
        if (dynamicStatus == AuctionStatus.PROCEEDING && auction.getEndTime() != null && auction.getEndTime().isBefore(java.time.LocalDateTime.now())) {
            dynamicStatus = AuctionStatus.COMPLETED;
        }

        return AuctionResponseDTO.builder()
                .id(auction.getId())
                .startPrice(auction.getStartPrice())
                .currentPrice(auction.getCurrentPrice())
                .startTime(auction.getStartTime())
                .endTime(auction.getEndTime())
                .status(dynamicStatus)
                .createdAt(auction.getCreatedAt())
                .updatedAt(auction.getUpdatedAt())
                .adminProduct(productDTO)
                .build();
    }
}
