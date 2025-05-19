package com.realive.dto.admin;


import java.time.LocalDateTime;

import lombok.Data;

@Data
public class AdminListDTO {
    
    private Integer id;

    private String email;

    private String name;
    
    private LocalDateTime createdAt;
    
}
