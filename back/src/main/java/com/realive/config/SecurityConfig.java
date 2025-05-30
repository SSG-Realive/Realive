package com.realive.config;

import java.util.List;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.realive.security.customer.CustomLoginSuccessHandler;
import com.realive.security.customer.CustomerJwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Configuration
@EnableMethodSecurity(prePostEnabled = true)
@Slf4j
@RequiredArgsConstructor
//@Order(2)
public class SecurityConfig {

    private final CustomerJwtAuthenticationFilter jwtAuthenticationFilter; // [Customer] JWT 인증 필터
    private final CustomLoginSuccessHandler customLoginSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("------------------Security Config-----------------------");

        http
            .securityMatcher("/api/customer/**", "/api/public/**")
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .formLogin(form -> form.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/public/**").permitAll() // [Customer] 공개 API
                .requestMatchers("/api/customer/**").authenticated() // [Customer] 로그인 사용자만 접근
                .anyRequest().authenticated())
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))) // [Customer] 모든 인증 실패 요청에 대해 401 Unauthorized 응답 반환
            .oauth2Login(config -> config.successHandler(customLoginSuccessHandler)); // [Customer] 소셜로그인 성공 핸들러
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

    //static 자원들 로그인 체크를 막는 설정 
    //import 주의!! - ..servlet..(X)
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {

    	log.info("------------web configure-------------------");

        //PathRequest impot 조심 -> servlet으로  
    	return (web) -> web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());

	}

    //CORS 설정(6버전)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration corsConfiguration = new CorsConfiguration();

        corsConfiguration.setAllowedOriginPatterns(List.of("*")); // 어디서든 허락
        corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS"));
        corsConfiguration.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type"));
        corsConfiguration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);

        return source;
    }

}
