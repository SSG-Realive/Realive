package com.realive.controller.public_api;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import com.realive.dto.customer.member.MemberJoinDTO;
import com.realive.dto.customer.member.MemberLoginDTO;
import com.realive.security.customer.JwtResponse;
import com.realive.security.customer.JwtTokenProvider;
import com.realive.service.customer.MemberService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

// [Customer,공개API] 일반 로그인 및 일반 회원가입 컨트롤러

@RestController
@RequestMapping("/api/public/auth")
@Log4j2
@RequiredArgsConstructor
public class LoginController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberService memberService;

    
    // 일반 로그인
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid MemberLoginDTO dto) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword())
        );

        String token = jwtTokenProvider.createToken(authentication);

        return ResponseEntity.ok(new JwtResponse(token));
    }

    // 일반 회원가입
    @PostMapping("/join")
    public ResponseEntity<?> registerMember(@RequestBody @Valid MemberJoinDTO dto) {
        String token = memberService.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                    "message", "회원가입 성공",
                    "token", token
                ));
    }

}
