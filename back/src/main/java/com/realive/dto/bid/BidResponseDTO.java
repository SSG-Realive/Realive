package com.realive.dto.bid;

import com.realive.domain.auction.Bid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 입찰 응답
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BidResponseDTO {

    private Integer id;
    private Integer auctionId;
    private Integer customerId;
    private Integer bidPrice;
    private LocalDateTime bidTime;
    private String customerName;

    public static BidResponseDTO fromEntity(Bid bid, String customerName) {
        return BidResponseDTO.builder()
                .id(bid.getId())
                .auctionId(bid.getAuctionId())
                .customerId(bid.getCustomerId())
                .bidPrice(bid.getBidPrice())
                .bidTime(bid.getBidTime())
                .customerName(customerName)
                .build();
    }
}
