package com.realive.dto.seller;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * SellerLoginRequestDTO
 * - 판매자 로그인 요청 시 클라이언트가 보내는 데이터 형식
 * - 이메일과 비밀번호를 포함함
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SellerLoginRequestDTO {

    private String email;       // 판매자 이메일 (로그인 ID로 사용)
    private String password;    // 판매자 비밀번호

}