package com.realive.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class AdminRequestDTO {
    
    @NotNull
    private Integer id;
    @NotBlank
    private String email;
    @NotBlank
    private String name;
}
