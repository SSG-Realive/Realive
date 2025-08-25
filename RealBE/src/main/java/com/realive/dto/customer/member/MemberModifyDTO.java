package com.realive.dto.customer.member;

import jakarta.validation.constraints.Email;
import lombok.Data;

import java.time.LocalDate;

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
