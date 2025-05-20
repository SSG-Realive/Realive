package com.realive.dto.admin;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminLoginResponseDTO {
    
    private String accessToken; 
    
    private String refreshToken; 
    
    private String name;

    private String message;

}
