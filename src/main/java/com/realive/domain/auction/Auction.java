package com.realive.domain.auction;

import com.realive.domain.common.BaseTimeEntity;
import com.realive.domain.common.enums.AuctionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "auctions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Auction extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "product_id", nullable = false)
    private Integer productId;

    @Column(name = "start_price", nullable = false)
    private Integer startPrice;

    @Column(name = "current_price", nullable = false)
    private Integer currentPrice;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private AuctionStatus status = AuctionStatus.PROCEEDING;

    @PrePersist
    protected void onCreate() {
        status = AuctionStatus.PROCEEDING;
    }

    public boolean isClosed() {
        return status == AuctionStatus.COMPLETED || status == AuctionStatus.CANCELLED;
    }
}