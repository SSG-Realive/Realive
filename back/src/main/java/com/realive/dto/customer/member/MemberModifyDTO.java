package com.realive.dto.customer.member;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// [Customer] 회원정보 수정DTO 

@Data
public class MemberModifyDTO {

    @NotBlank(message = "이름을 입력해주세요.")
    private String name;

    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "이메일 형식을 확인해주세요.")
    private String email;

    @NotBlank(message = "핸드폰 번호를 입력해주세요.")
    private String phone;

    @NotBlank(message = "주소를 입력해주세요.")
    private String address;

    @NotNull(message = "생일을 입력해주세요.")
    private LocalDate birth;
    
}
