package com.realive.dto.admin;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;



@Getter
@AllArgsConstructor
public class AdminReadDTO {

    private Integer id;

    private String email;

    private String name;

    private String authorities;

    private LocalDateTime createdAt;  //?
    
}
