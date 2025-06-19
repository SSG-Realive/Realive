package com.realive.controller.public_api;


import com.realive.domain.customer.Customer;
import com.realive.dto.customer.login.CustomerLoginRequestDTO;
import com.realive.dto.customer.login.CustomerLoginResponseDTO;
import com.realive.repository.customer.CustomerRepository;
import com.realive.security.JwtUtil;
import com.realive.security.customer.CustomerPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/auth")
@Slf4j
// ✅ @RequiredArgsConstructor를 사용하려면 필드를 final로 선언해야 합니다.
@RequiredArgsConstructor
public class LoginController {

    // --- 의존성 주입 부분 ---
    // ✅ @Qualifier는 주입받는 필드 또는 생성자 파라미터에 사용합니다.
    // 여러 UserDetailsService 중 "customUserDetailsService"라는 이름을 가진 Bean을 특정해서 주입받습니다.
    @Qualifier("customUserDetailsService")
    private final UserDetailsService customUserDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;


    @PostMapping("/login")
    public ResponseEntity<CustomerLoginResponseDTO> login(@RequestBody @Valid CustomerLoginRequestDTO request) {
        log.info("--- [테스트] 고객 로그인 수동 인증 시작 ---");

        try {
            // 1. UserDetailsService를 직접 호출하여 DB에서 사용자 정보를 가져옵니다.
            log.info("1. UserDetailsService로 사용자 찾기 시도: {}", request.email());
            CustomerPrincipal principal = (CustomerPrincipal) customUserDetailsService.loadUserByUsername(request.email());
            log.info("✅ 1-1. 사용자 찾기 성공! 이름: {}", principal.getName());
            String rawPasswordFromRequest = request.password();
        String encodedPasswordFromDB = principal.getPassword();
        
        log.info("-------------------------------------------");
        log.info("--- 최종 비밀번호 비교 디버깅 ---");
        log.info("프론트에서 받은 비밀번호 원본: [{}]", rawPasswordFromRequest);
        log.info("DB에서 가져온 암호화된 비밀번호: [{}]", encodedPasswordFromDB);
        
        boolean isMatch = passwordEncoder.matches(rawPasswordFromRequest, encodedPasswordFromDB);
        log.info("passwordEncoder.matches() 결과: {}", isMatch);
        log.info("-------------------------------------------");
            // 2. PasswordEncoder를 직접 호출하여 비밀번호를 비교합니다.
            log.info("2. 비밀번호 비교 시도...");
            if (!passwordEncoder.matches(request.password(), principal.getPassword())) {
                log.error("❌ 2-1. 비밀번호 불일치!");
                throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
            }
            log.info("✅ 2-1. 비밀번호 일치!");

            // 3. 토큰을 생성합니다.
            log.info("3. JWT 토큰 생성 시도...");
            String accessToken = jwtUtil.generateAccessToken(principal);
            String refreshToken = jwtUtil.generateRefreshToken(principal);
            log.info("✅ 3-1. 토큰 생성 성공!");

            // 4. 성공 응답을 반환합니다.
            CustomerLoginResponseDTO responseDto = CustomerLoginResponseDTO.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .email(principal.getUsername())
                    .name(principal.getName())
                    .build();

            log.info("--- [테스트] 고객 로그인 성공 ---");
            return ResponseEntity.ok(responseDto);

        } catch (Exception e) {
            log.error("--- [테스트] 인증 과정 중 에러 발생 ---", e);
            throw new BadCredentialsException("인증 실패: " + e.getMessage());
        }
    }




}