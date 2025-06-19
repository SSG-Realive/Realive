package com.realive.dto.seller;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * SellerSignupDTO
 * - 판매자 회원가입 요청을 처리하기 위한 DTO 클래스
 * - 회원가입 시 입력받는 필드에 대한 유효성 검증 포함
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerSignupDTO {

    @NotBlank
    private String email;               // 판매자 이메일 (로그인 ID로 사용됨)

    @NotBlank
    private String name;                // 판매자 이름 또는 상호명

    @NotBlank
    private String phone;               // 판매자 전화번호

    @NotBlank
    private String password;            // 비밀번호 (암호화하여 저장됨)

    @NotBlank
    private String businessNumber;      // 사업자 등록번호

}