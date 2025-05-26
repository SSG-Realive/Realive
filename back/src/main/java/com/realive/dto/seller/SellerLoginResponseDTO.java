package com.realive.dto.seller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * SellerLoginResponseDTO
 * - 판매자 로그인 성공 시 서버가 클라이언트에 응답하는 데이터 형식
 * - JWT 토큰 및 판매자 정보 포함
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerLoginResponseDTO {

    private String accessToken;     // 액세스 토큰 (JWT) - 인증에 사용됨
    private String refreshToken;    // 리프레시 토큰 - 액세스 토큰 갱신 시 사용됨
    private String email;           // 로그인한 판매자의 이메일
    private String name;            // 로그인한 판매자의 이름

}