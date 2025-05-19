package com.realive.dto.auction;

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
    private Integer productId;
    private Integer startPrice;
    private Integer currentPrice;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Boolean isClosed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private AdminProductDTO adminProduct;

    public static AuctionResponseDTO fromEntity(com.realive.domain.auction.Auction auction, AdminProductDTO productDTO) {
        return AuctionResponseDTO.builder()
                .id(auction.getId())
                .productId(auction.getProductId())
                .startPrice(auction.getStartPrice())
                .currentPrice(auction.getCurrentPrice())
                .startTime(auction.getStartTime())
                .endTime(auction.getEndTime())
                .isClosed(auction.isClosed())
                .createdAt(auction.getCreatedAt())
                .updatedAt(auction.getUpdatedAt())
                .adminProduct(productDTO)
                .build();
    }
}
