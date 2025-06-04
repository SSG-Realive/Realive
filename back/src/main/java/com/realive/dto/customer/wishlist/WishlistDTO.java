package com.realive.dto.customer.wishlist;

import lombok.Data;

// [Customer] 위시리스트DTO 

@Data
public class WishlistDTO {
    
    private Long wishlistId;

    private Long customerId;

    private Long productId;

}
