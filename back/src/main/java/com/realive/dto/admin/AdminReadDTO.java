package com.realive.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class AdminReadDTO {
    private Integer id;

    private String email;

    private String name;

    private LocalDateTime createdAt;
}

    

