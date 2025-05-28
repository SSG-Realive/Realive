package com.realive.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminInfoResponseDTO {
    private String name;
    private String email;
    private String role;
}
