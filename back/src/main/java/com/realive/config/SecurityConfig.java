package com.realive.config;

import com.realive.security.AdminJwtAuthenticationFilter;
import com.realive.security.seller.SellerJwtAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;

import com.realive.security.customer.CustomerJwtAuthenticationFilter;
import com.realive.security.customer.CustomLoginSuccessHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
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
    private final CustomLoginSuccessHandler customLoginSuccessHandler;


    @Autowired
    @Qualifier("customUserDetailsService")
    private UserDetailsService customUserDetailsService;

    @Autowired
    @Qualifier("adminDetailsService")
    private UserDetailsService adminDetailsService;

    // Provider를 명시적으로 등록
    // customerAuthProvider, adminAuthProvider를 직접 수동 생성해 ProviderManager에 주입
    // Spring Security가 내부적으로 자동 설정X

    // Admin 인증 Provider
    @Bean
    public DaoAuthenticationProvider adminAuthProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(adminDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // Customer 인증 Provider
    @Bean
    public DaoAuthenticationProvider customerAuthProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // AuthenticationManager에 명시적으로 등록
    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(List.of(customerAuthProvider(), adminAuthProvider()));
    }

    // === Admin Security Chain ===
    @Bean
    @Order(1)
    public SecurityFilterChain adminSecurityFilterChain(HttpSecurity http) throws Exception {
        log.info("Admin SecurityConfig 적용");

        // AdminJwtAuthenticationFilter 직접 생성
        AdminJwtAuthenticationFilter adminJwtAuthenticationFilter = new AdminJwtAuthenticationFilter(jwtUtil);

        http
                .securityMatcher("/api/admin/**")
                .authenticationManager(authenticationManager()) // 명시적 연결
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/admin/login").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(adminJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // === Seller Security Chain ===
    @Bean
    @Order(2)
    public SecurityFilterChain sellerSecurityFilterChain(HttpSecurity http) throws Exception {
        log.info("Seller SecurityConfig 적용");

        http
                .securityMatcher("/api/seller/**")
                .authenticationManager(authenticationManager())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/seller/login", "/api/seller/signup").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(sellerJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // === Customer + Public Security Chain ===
    @Bean
    @Order(3)
    public SecurityFilterChain customerSecurityFilterChain(HttpSecurity http) throws Exception {
        log.info("Customer SecurityConfig 적용");

        http
                .securityMatcher("/api/**") // 나머지 API
                .authenticationManager(authenticationManager())
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/api/customer/**").authenticated()
                        .anyRequest().denyAll()
                )
                .oauth2Login(config -> config.successHandler(customLoginSuccessHandler))
                .addFilterBefore(customerJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
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

        corsConfiguration.setAllowedOriginPatterns(List.of("http://localhost:3000"));
        corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS"));
        corsConfiguration.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type"));
        corsConfiguration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);

        return source;
    }
}