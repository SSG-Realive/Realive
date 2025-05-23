package com.realive.config;

import com.realive.security.AdminJwtAuthenticationFilter;
import com.realive.security.JwtAuthenticationFilter;
import com.realive.security.JwtUtil;
import com.realive.service.admin.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

    private final JwtUtil jwtUtil;

    // JwtUtil만 생성자 주입 (필요시 @Lazy 가능)
    public SecurityConfig(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    // JwtAuthenticationFilter를 직접 Bean으로 등록
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(@Lazy AdminService adminService) {
        return new JwtAuthenticationFilter(jwtUtil, adminService);
    }

    // AdminJwtAuthenticationFilter도 Bean으로 등록
    @Bean
    public AdminJwtAuthenticationFilter adminJwtAuthenticationFilter(@Lazy AdminService adminService) {
        return new AdminJwtAuthenticationFilter(jwtUtil, adminService);
    }

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
                        .requestMatchers("/**").permitAll()
                )
                .addFilterBefore(adminJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();  // 개발용. 운영시 BCrypt로 교체 권장
    }
}
