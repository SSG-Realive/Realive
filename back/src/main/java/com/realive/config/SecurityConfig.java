package com.realive.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.realive.security.JwtAuthenticationFilter;

/**
 * Spring Security 설정 클래스
 * - JWT 인증 필터 적용
 * - 패스워드 인코더 설정
 * - CORS/CSRF 및 인증 관련 설정 구성
 */
@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

    // JWT 인증 필터 주입
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Spring Security 필터 체인 설정
     * - httpBasic, csrf 비활성화
     * - 모든 요청에 대해 허용 (개발 시 오픈, 이후 제한 필요)
     * - UsernamePasswordAuthenticationFilter 앞에 JWT 필터 등록
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("Security Filter Chain");

        return http
                // 기본 HTTP Basic 인증 비활성화
                .httpBasic(httpBasic -> httpBasic.disable())

                // CSRF 비활성화 (JWT 기반 API 서버에서는 일반적으로 사용하지 않음)
                .csrf(csrf -> csrf.disable())

                // 요청 권한 설정 (현재는 모든 요청 허용)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/*").permitAll()
                        .anyRequest().permitAll()
                )

                // 사용자 이름/비밀번호 인증 필터 앞에 JWT 필터를 추가
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // SecurityFilterChain 객체로 반환
                .build();
    }

    /**
     * 비밀번호 암호화를 위한 BCryptPasswordEncoder 빈 등록
     * - 사용자 패스워드 저장 시 해시화 처리
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}