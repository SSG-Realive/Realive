package com.realive.domain.auction;

import com.realive.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "bids")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bid extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "auction_id")
    private Integer auctionId;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "bid_price")
    private Integer bidPrice;

    @Column(name = "bid_time")
    private LocalDateTime bidTime;
}