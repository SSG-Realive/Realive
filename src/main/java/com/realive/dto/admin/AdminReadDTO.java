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

    private LocalDateTime createdAt;
}

    

