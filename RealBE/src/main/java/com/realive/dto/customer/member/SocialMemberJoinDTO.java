package com.realive.dto.customer.member;

import java.time.LocalDate;
import com.realive.domain.customer.Gender;
import jakarta.validation.constraints.*;
import lombok.Data;

// [customer] 소셜회원가입용 DTO

@Data
public class SocialMemberJoinDTO {

    // 소셜 로그인에서는 이메일이 이미 검증되어 있으므로 별도 검증 불필요
    private String email;

    // 소셜 로그인에서 비밀번호는 선택사항 (또는 아예 없을 수 있음)
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

    // 이메일 인증 코드는 소셜 회원가입에서 불필요
}
