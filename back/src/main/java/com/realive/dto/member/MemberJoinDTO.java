package com.realive.dto.member;

import java.time.LocalDate;
import com.realive.domain.customer.Gender;
import lombok.Data;

//회원-회원가입용 
@Data
public class MemberJoinDTO {

    private String email;

    private String password;

    private String name;

    private String phone;

    private String address;

    private LocalDate birth;

    private Gender gender; 
    
}
