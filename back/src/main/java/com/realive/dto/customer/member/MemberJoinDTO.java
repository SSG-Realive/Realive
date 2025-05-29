package com.realive.dto.customer.member;

import java.time.LocalDate;
import com.realive.domain.customer.Gender;
import lombok.Data;
import lombok.Setter;

// [Customer] 회원가입DTO

@Data
@Setter
public class MemberJoinDTO {

    private String email;

    private String password;

    private String name;

    private String phone;

    private String address;

    private LocalDate birth;

    private Gender gender; 

    private LocalDate created; // 가입일자
    
}
