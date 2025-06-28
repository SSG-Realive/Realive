package com.realive.controller.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.realive.dto.auth.EmailRequestDTO;
import com.realive.dto.customer.member.MemberJoinDTO;
import com.realive.service.auth.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public/auth")
public class AuthController {
    record ApiResp(boolean success, String message) {}
    private final AuthService authService;

    // 1. 이메일 인증 코드 발송
    @PostMapping("/send-verification")
    public ResponseEntity<ApiResp> sendVerificationCode(
            @RequestBody @Valid EmailRequestDTO dto) {

        authService.sendVerificationCode(dto.email());

        return ResponseEntity.ok(
            new ApiResp(true, "인증 코드가 성공적으로 발송되었습니다."));
    }

    // 2. 최종 회원가입
    @PostMapping("/signup")
    public ResponseEntity<ApiResp> signUp(
            @RequestBody @Valid MemberJoinDTO dto) {

        authService.signUpWithVerification(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResp(true, "회원가입이 성공적으로 완료되었습니다."));
    }
}

