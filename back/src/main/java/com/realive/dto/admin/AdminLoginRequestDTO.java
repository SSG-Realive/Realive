package com.realive.dto.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminLoginRequestDTO {

    @Email @NotBlank
    private String email;

    @NotBlank
    private String password;
}