package com.realive.dto.customer.member;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// [Customer] 회원정보 수정DTO 

@Data
public class MemberModifyDTO {

    private String name;

    @Email(message = "이메일 형식을 확인해주세요.")
    private String email;

    private String phone;

    private String address;

    private LocalDate birth;
    
}
