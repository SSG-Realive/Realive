package com.realive.dto.wishlist;

import com.realive.domain.customer.Customer;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class WishlistDTO {
    
    private Long wishlistId;

    private Long customerId;

    private Long productId;


}
