package com.realive.dto.admin;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminListDTO {
    
    private Integer id;

    private String email;

    private String name;
    
    private LocalDateTime createdAt;

    AdminListDTO dto = AdminListDTO.builder()
    .id(1)
    .email("admin@domain.com")
    .name("관리자")
    .createdAt(LocalDateTime.now())
    .build();
    
}
