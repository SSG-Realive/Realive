package com.realive.dto.customer.member;

import com.realive.domain.customer.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

// [Customer] 회원가입DTO

@Data
public class MemberJoinDTO {

    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "이메일 형식을 확인해주세요.")
    private String email;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$",
        message = "비밀번호는 영문자와 숫자를 포함하여 8자 이상이어야 합니다."
    )
    private String password;

    @NotBlank(message = "이름을 입력해주세요.")
    private String name;

    @NotBlank(message = "핸드폰 번호를 입력해주세요.")
    private String phone;

    @NotBlank(message = "주소를 입력해주세요.")
    private String address;

    @NotNull(message = "생일을 입력해주세요.")
    private LocalDate birth;

    @NotNull(message = "성별을 입력해주세요.")
    private Gender gender; 

      // ✅ 이메일 인증 코드를 받을 필드를 추가합니다.
    @NotBlank(message = "이메일 인증 코드를 입력해주세요.")
    @Size(min = 6, max = 6, message = "인증 코드는 6자리입니다.")
    private String verificationCode;

}
