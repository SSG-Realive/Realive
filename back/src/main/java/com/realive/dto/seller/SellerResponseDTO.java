package com.realive.dto.seller;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerResponseDTO {

    private String email;
    private String name;
    private String phone;
    
    private boolean isApproved;
    
    private String businessNumber;

    private boolean hasBankAccountCopy;
 
    
    
}
