package com.realive.config;

import com.realive.repository.seller.SellerRepository;
import com.realive.security.AdminJwtAuthenticationFilter;
import com.realive.security.JwtAuthenticationFilter;
import com.realive.security.JwtUtil;
import com.realive.service.admin.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtUtil jwtUtil;

    // JwtUtil만 생성자 주입
    public SecurityConfig(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    // JwtAuthenticationFilter Bean 등록 (일반 사용자용)
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(SellerRepository sellerRepository) {
        return new JwtAuthenticationFilter(jwtUtil, sellerRepository);
    }

    // AdminJwtAuthenticationFilter Bean 등록 (관리자 인증용)
    @Bean
    public AdminJwtAuthenticationFilter adminJwtAuthenticationFilter(@Lazy AdminService adminService) {
        return new AdminJwtAuthenticationFilter(jwtUtil, adminService);
    }

    // Security 필터 체인 통합 설정
    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            AdminJwtAuthenticationFilter adminJwtAuthenticationFilter
    ) throws Exception {
        log.info("Security Filter Chain");

        return http
                .httpBasic(httpBasic -> httpBasic.disable())
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/admin/login").permitAll()
                        .requestMatchers("/api/admin/**").authenticated()
                        .anyRequest().permitAll()
                )
                // 관리자 인증 필터 우선 적용, 그 다음 일반 사용자 인증 필터 적용 (순서 중요!)
                .addFilterBefore(adminJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    // 비밀번호 암호화용 Encoder 등록
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}