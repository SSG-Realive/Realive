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

import com.realive.dto.member.MemberJoinDTO;
import com.realive.dto.member.MemberLoginDTO;
import com.realive.security.JwtResponse;
import com.realive.security.JwtTokenProvider;
import com.realive.service.customer.MemberService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

// 일반 로그인 및 일반 회원가입 컨트롤러

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
    public ResponseEntity<?> login(@RequestBody MemberLoginDTO dto) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword())
        );

        String token = jwtTokenProvider.createToken(authentication);

        return ResponseEntity.ok(new JwtResponse(token));
    }

    // 일반 회원가입
    @PostMapping("/join")
    public ResponseEntity<?> registerMember(@RequestBody MemberJoinDTO dto) {
        try {
            String token = memberService.register(dto);
            // 가입 직후 자동 로그인용 JWT 토큰을 반환할 수도 있고, 단순 메시지를 줄 수도 있습니다.
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(Map.of(
                        "message", "회원가입 성공",
                        "token", token
                    ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    
}
