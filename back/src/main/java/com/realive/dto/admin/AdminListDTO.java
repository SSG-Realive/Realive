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
    
}
