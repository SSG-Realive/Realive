package com.realive.dto.member;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.realive.domain.customer.Gender;

import lombok.Data;

//회원-회원정보조회
@Data
public class MemberReadDTO {

    private String name;

    private String email;

    private String phone;

    private String address;

    private LocalDate birth;

    private LocalDateTime created;
    
}
