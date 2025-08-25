package com.realive.controller.auth;

import com.realive.service.auth.PasswordResetService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 비밀번호 재설정 흐름을 위한 REST API.
 *
 * • POST   /api/auth/password-reset/request  → 인증번호 발송
 * • POST   /api/auth/password-reset/verify   → 코드 검증
 * • POST   /api/auth/password-reset/confirm  → 비밀번호 변경
 */
@RestController
@RequestMapping("/api/public/password-reset")
@RequiredArgsConstructor
@Validated
public class PasswordResetController {

    private final PasswordResetService service;

    /**
     * 1️⃣ 인증번호 발송
     *
     * Request Body:
     * {
     *   "email": "user@example.com"
     * }
     *
     * • 가입된 이메일이 아니면 400 에러.
     * • 성공 시 HTTP 204(No Content).
     */
    @PostMapping("/request")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void requestReset(@RequestBody @Valid PasswordResetRequest req) {
        service.requestReset(req.email());
    }

    /**
     * 2️⃣ 인증번호 검증
     *
     * Request Body:
     * {
     *   "email": "user@example.com",
     *   "code": "123456"
     * }
     *
     * • 코드 불일치/만료 시 400 에러.
     * • 성공 시 HTTP 204(No Content).
     */
    @PostMapping("/verify")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void verifyCode(@RequestBody @Valid PasswordResetVerifyRequest req) {
        service.verifyCode(req.email(), req.code());
    }

    /**
     * 3️⃣ 비밀번호 변경(확인)
     *
     * Request Body:
     * {
     *   "email": "user@example.com",
     *   "newPassword": "새비밀번호",
     *   "confirmPassword": "새비밀번호"
     * }
     *
     * • 비밀번호 확인 불일치/인증 필요/토큰 만료 시 400 에러.
     * • 성공 시 HTTP 204(No Content).
     */
    @PostMapping("/confirm")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void confirmReset(@RequestBody @Valid PasswordResetConfirmRequest req) {
        service.confirmReset(
            req.email(),
            req.newPassword(),
            req.confirmPassword()
        );
    }

    // ───────────────────────────────────────────
    // DTO: Request Body 바인딩용 Record 클래스
    // ───────────────────────────────────────────

    public static record PasswordResetRequest(
        @Email
        @NotBlank String email
    ) { }

    public static record PasswordResetVerifyRequest(
        @Email
        @NotBlank String email,

        @NotBlank
        String code
    ) { }

    public static record PasswordResetConfirmRequest(
        @Email
        @NotBlank String email,

        @NotBlank
        String newPassword,

        @NotBlank
        String confirmPassword
    ) { }

}
