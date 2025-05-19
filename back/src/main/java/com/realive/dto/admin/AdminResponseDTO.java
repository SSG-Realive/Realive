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

    AdminResponseDTO dto = AdminResponseDTO.builder()
    .id(1)
    .email("admin@domain.com")
    .name("관리자")
    .message(" ")
    .build();
}
