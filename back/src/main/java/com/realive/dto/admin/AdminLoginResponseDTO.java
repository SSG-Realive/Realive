package com.realive.dto.admin;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminLoginResponseDTO {
    
    private Integer id;

    private String email;

    private String name;

    private String message;

}
