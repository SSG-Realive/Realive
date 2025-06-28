package com.realive.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenResponseDTO {

    private String grantType; // 토큰 타입 (예: "Bearer")

    private String accessToken;

    private String refreshToken;

    private Long accessTokenExpiresIn; // Access Token 만료 시간 (밀리초)
}
