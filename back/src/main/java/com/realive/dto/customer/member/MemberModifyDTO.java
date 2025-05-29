package com.realive.dto.customer.member;

import java.time.LocalDate;

import lombok.Data;

// [Customer] 회원정보 수정DTO 

@Data
public class MemberModifyDTO {
    
    private Long id;

    private String name;

    private String email;

    private String phone;

    private String address;

    private LocalDate birth;
    
}
