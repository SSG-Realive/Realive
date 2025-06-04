package com.realive.dto.customer.member;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

// [Customer] 회원정보 조회DTO

@Data
public class MemberReadDTO {

    private String name;

    private String email;

    private String phone;

    private String address;

    private LocalDate birth;

    private LocalDateTime created;
    
}
