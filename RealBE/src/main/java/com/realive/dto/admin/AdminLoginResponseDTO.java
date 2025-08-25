package com.realive.dto.admin;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminLoginResponseDTO {

    private String accessToken;
    private String refreshToken;
    private String email;
    private String name;
}
