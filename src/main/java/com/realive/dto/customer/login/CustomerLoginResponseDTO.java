package com.realive.dto.customer.login;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CustomerLoginResponseDTO {

    private String accessToken;
    private String refreshToken;
    private String email;
    private String name;
    
}
