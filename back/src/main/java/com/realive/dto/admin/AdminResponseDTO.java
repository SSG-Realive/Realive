package com.realive.dto.admin;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminResponseDTO {
    
    private Integer id;

    private String email;

    private String name;

    @NotBlank
    private String message;

}
