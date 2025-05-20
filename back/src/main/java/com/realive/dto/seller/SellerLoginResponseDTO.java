package com.realive.dto.seller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerLoginResponseDTO {

    private String accessToken;
    private String refreshToken;
    private String email;
    private String name;
    
}
