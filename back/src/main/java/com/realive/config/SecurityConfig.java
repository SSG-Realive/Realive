package com.realive.config;

import com.realive.security.AdminJwtAuthenticationFilter;
import com.realive.security.seller.SellerJwtAuthenticationFilter;
import com.realive.security.customer.CustomerJwtAuthenticationFilter;
import com.realive.security.customer.CustomLoginSuccessHandler;
import com.realive.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@Configuration
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final CustomerJwtAuthenticationFilter customerJwtAuthenticationFilter;
    private final SellerJwtAuthenticationFilter sellerJwtAuthenticationFilter;
    private final AdminJwtAuthenticationFilter adminJwtAuthenticationFilter;
    private final CustomLoginSuccessHandler customLoginSuccessHandler;
    private final JwtUtil jwtUtil;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("✅ 통합 SecurityConfig 적용");

        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .formLogin(form -> form.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // 관리자 엔드포인트
                .requestMatchers("/api/admin/login").permitAll()
                .requestMatchers("/api/admin/**").authenticated()
                // 판매자 엔드포인트
                .requestMatchers("/api/seller/login").permitAll()
                .requestMatchers("/api/seller/signup").permitAll()
                .requestMatchers("/api/seller/**").authenticated()
                // 고객 엔드포인트
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/customer/**").authenticated()
            )
            .oauth2Login(config -> config
                .successHandler(customLoginSuccessHandler));  

        // 각 사용자 유형별 필터 추가 (순서 중요)
        http.addFilterBefore(adminJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(sellerJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(customerJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        log.info("✅ 정적 리소스 보안 설정 적용");
    	return (web) -> web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());
	}

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();

        corsConfiguration.setAllowedOriginPatterns(List.of("*"));
        corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS"));
        corsConfiguration.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type"));
        corsConfiguration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);

        return source;
    }
}
