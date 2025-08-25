package com.realive.domain.auction;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import com.realive.dto.auction.AdminProductDTO;

@Entity
@Table(name = "admin_products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "product_id")
    private Integer productId;

    @Column(name = "purchase_price")
    private Integer purchasePrice;

    @Column(name = "purchased_from_seller_id")
    private Integer purchasedFromSellerId;

    @Column(name = "purchased_at")
    private LocalDateTime purchasedAt;

    @Column(name = "is_auctioned")
    private boolean isAuctioned;

    

}

