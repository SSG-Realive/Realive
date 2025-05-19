package com.realive.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class AdminRequestDTO {
    
    @NotNull
    private Integer id;
    @NotBlank
    private String email;
    @NotBlank
    private String name;

    AdminRequestDTO dto = AdminRequestDTO.builder()
    .id(1)
    .email("admin@domain.com")
    .name("관리자")
    .build();
}
