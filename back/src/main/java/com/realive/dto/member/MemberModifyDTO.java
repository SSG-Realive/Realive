package com.realive.dto.member;

import java.time.LocalDate;

import lombok.Data;

//회원-회원정보 수정/삭제 
@Data
public class MemberModifyDTO {
    
    private Long id;

    private String name;

    private String email;

    private String phone;

    private String address;

    private LocalDate birth;
    
}
