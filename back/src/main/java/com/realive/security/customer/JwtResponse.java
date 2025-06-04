package com.realive.security.customer;

import lombok.AllArgsConstructor;
import lombok.Data;

// [Customer] JWT 인증 후 클라이언트 응답 객체

@Data
@AllArgsConstructor
public class JwtResponse {

    private String token;

}