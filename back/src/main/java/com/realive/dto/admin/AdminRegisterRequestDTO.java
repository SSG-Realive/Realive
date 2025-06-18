package com.realive.dto.admin;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminRegisterRequestDTO {
    private String name;
    private String email;
    private String password;
}