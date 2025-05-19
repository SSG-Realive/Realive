package com.realive.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminResponseDTO {
    
    private Integer id;

    private String email;

    private String name;

    @NotBlank
    private String message;
}
