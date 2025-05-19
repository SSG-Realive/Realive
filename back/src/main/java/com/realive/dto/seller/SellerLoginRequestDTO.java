package com.realive.dto.seller;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SellerLoginRequestDTO {

    private String email;
    private String password;
    
}
