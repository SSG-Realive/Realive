package com.realive.dto.seller;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * SellerUpdateDTO
 * - 판매자 정보 수정 요청을 처리하기 위한 DTO 클래스
 * - 필수 정보: 이름(name), 전화번호(phone)
 * - 선택 정보: 비밀번호 변경 시 사용하는 새 비밀번호(newPassword)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerUpdateDTO {

    @NotBlank
    private String name;            // 판매자 이름 (필수 입력)

    @NotBlank
    private String phone;           // 판매자 전화번호 (필수 입력

    private String newPassword;     // 변경할 새 비밀번호 (선택 입력)

}