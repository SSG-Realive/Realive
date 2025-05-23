package com.realive.config;

import com.realive.security.AdminJwtAuthenticationFilter;
import com.realive.security.JwtAuthenticationFilter;
import com.realive.security.JwtUtil;
import com.realive.service.admin.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // 생성자 주입에서 AdminService, JwtUtil 빼기!
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    // AdminJwtAuthenticationFilter는 @Bean 메소드에서 직접 필요한 의존성을 주입받음
    @Bean
    public AdminJwtAuthenticationFilter adminJwtAuthenticationFilter(AdminService adminService, JwtUtil jwtUtil) {
        return new AdminJwtAuthenticationFilter(jwtUtil, adminService);
    }

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            AdminJwtAuthenticationFilter adminJwtAuthenticationFilter   // 여기서 주입받기!
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
                // 관리용 JWT 필터를 먼저 적용
                .addFilterBefore(adminJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
        // 운영시: return new BCryptPasswordEncoder();
    }
}