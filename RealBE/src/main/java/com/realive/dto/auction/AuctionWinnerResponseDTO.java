package com.realive.dto.auction;

import com.realive.domain.auction.Auction;
import com.realive.domain.auction.Bid;
import com.realive.domain.common.enums.AuctionStatus;
import lombok.*;

import java.time.LocalDateTime;

// 낙찰자 전용 상세 응답 DTO
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuctionWinnerResponseDTO {

    private Integer id;
    private Integer startPrice;
    private Integer finalPrice;        // 낙찰가
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private AuctionStatus status;
    private String statusText;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private AdminProductDTO adminProduct;

    // 추가: 낙찰자 정보
    private LocalDateTime bidTime;     // 마지막(낙찰) 입찰 시각
    private LocalDateTime paymentDue;   // 결제 마감 시각 (예: endTime + 24h)
    
    // 추가: 결제 상태 정보
    private boolean isPaid;            // 결제 완료 여부
    private String paymentStatus;      // 결제 상태 텍스트

    public static AuctionWinnerResponseDTO fromEntity(
            Auction auction,
            AdminProductDTO productDTO,
            Bid winningBid
    ) {
        // 동적 상태 계산 (종료 후 자동 COMPLETED 처리)
        AuctionStatus dynamicStatus = auction.getStatus();
        if (dynamicStatus == AuctionStatus.PROCEEDING
                && auction.getEndTime() != null
                && auction.getEndTime().isBefore(LocalDateTime.now())) {
            dynamicStatus = AuctionStatus.COMPLETED;
        }
        String statusText = AuctionResponseDTO.getStatusText(dynamicStatus, auction.getStartTime());

        return AuctionWinnerResponseDTO.builder()
                .id(auction.getId())
                .startPrice(auction.getStartPrice())
                .finalPrice(winningBid.getBidPrice())
                .startTime(auction.getStartTime())
                .endTime(auction.getEndTime())
                .status(dynamicStatus)
                .statusText(statusText)
                .createdAt(auction.getCreatedAt())
                .updatedAt(auction.getUpdatedAt())
                .adminProduct(productDTO)
                .bidTime(winningBid.getBidTime())
                .paymentDue(auction.getEndTime().plusHours(24))
                .build();
    }
}
