package com.realive.domain.verification;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class VerificationCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email; // 인증 코드를 요청한 이메일

    @Column(nullable = false)
    private String code; // 발송된 인증 코드

    @Column(nullable = false)
    private LocalDateTime expiryDate; // 코드 만료 시간

    public VerificationCode(String email, String code, LocalDateTime expiryDate) {
        this.email = email;
        this.code = code;
        this.expiryDate = expiryDate;
    }

    // 사용자가 새 코드를 요청할 경우, 기존 코드를 업데이트하기 위한 메소드
    public void updateCode(String code, LocalDateTime expiryDate) {
        this.code = code;
        this.expiryDate = expiryDate;
    }
}