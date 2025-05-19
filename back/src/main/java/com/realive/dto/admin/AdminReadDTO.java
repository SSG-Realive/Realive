package com.realive.dto.admin;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class AdminReadDTO {

    private Integer id;

    private String email;

    private String name;

    private String authorities;

    private LocalDateTime createdAt; 
    
    AdminReadDTO dto = AdminReadDTO.builder()
    .id(1)
    .email("admin@domain.com")
    .name("관리자")
    .authorities("권한한")
    .createdAt(LocalDateTime.now())
    .build();
}
    

