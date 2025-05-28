package com.realive.dto.wishlist;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WishlistMostResponseDTO {

    private Long productId;

    private Long wishCounts;

    private String productName;

    private String productDetailUrl;

    private String thumbnailUrl;
}
