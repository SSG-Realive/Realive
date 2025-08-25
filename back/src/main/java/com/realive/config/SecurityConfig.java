package com.realive.config;

import com.realive.security.AdminJwtAuthenticationFilter;
import com.realive.security.SellerJwtAuthenticationFilter;
import com.realive.security.customer.CustomAuthorizationRequestResolver;
import com.realive.security.customer.CustomLoginSuccessHandler;
import com.realive.security.customer.CustomerJwtAuthenticationFilter;
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
    private final AdminJwtAuthenticationFilter adminJwtAuthenticationFilter;
    private final CustomLoginSuccessHandler customLoginSuccessHandler;

    @Autowired
    @Qualifier("customUserDetailsService")
    private UserDetailsService customUserDetailsService;

    @Autowired
    @Qualifier("adminDetailsService")
    private UserDetailsService adminDetailsService;

    @Autowired
    @Qualifier("sellerDetailsService")
    private UserDetailsService sellerDetailsService;

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

    @Bean
    public DaoAuthenticationProvider sellerAuthProvider() {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(sellerDetailsService);
    provider.setPasswordEncoder(passwordEncoder());
    return provider;
}

    // AuthenticationManager에 명시적으로 등록
    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(List.of(customerAuthProvider(), adminAuthProvider(),sellerAuthProvider()));
    }

    // === Admin Security Chain ===
    @Bean
    @Order(1)
    public SecurityFilterChain adminSecurityFilterChain(HttpSecurity http) throws Exception {
        log.info("Admin SecurityConfig 적용");

        http
                .securityMatcher("/api/admin/**")
                .authenticationManager(authenticationManager()) // 명시적 연결
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/admin/login","/api/admin/register").permitAll()
                        .anyRequest().hasAuthority("ROLE_ADMIN")
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
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))  // ★ 추가!
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/seller/login", "/api/seller/signup", "/api/seller/categories").permitAll()
                        .anyRequest().hasAuthority("ROLE_SELLER")
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
                .securityMatcher("/api/customer/**", "/api/public/**","/api/auth/**") // 나머지 API
                .authenticationManager(authenticationManager())
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/public/**", "/api/auth/**").permitAll()
                        .requestMatchers("/api/oauth2/**").permitAll()
                        .requestMatchers("/api/customer/update-info").hasAnyAuthority("ROLE_CUSTOMER", "ROLE_USER")
                        .requestMatchers("/api/customer/**").hasAnyAuthority("ROLE_CUSTOMER", "ROLE_USER")
                        .anyRequest().denyAll()
                )
                .addFilterBefore(customerJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // === Customer oauth2 Security Chain ===
    @Bean
    @Order(4)
    public SecurityFilterChain oauth2SecurityFilterChain(HttpSecurity http, CustomAuthorizationRequestResolver customResolver) throws Exception {
        http
            .securityMatcher("/oauth2/**", "/login/oauth2/**")
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .csrf(csrf -> csrf.disable())
            .oauth2Login(config -> config
                .authorizationEndpoint(endpoint -> endpoint
                    .authorizationRequestResolver(customResolver)
                )
                .successHandler(customLoginSuccessHandler)
                .failureHandler((request, response, exception) -> {
                    log.error("OAuth2 로그인 실패: {}", exception.getMessage(), exception);
                    response.sendRedirect("/login?error=kakao_login_failed");
                })
            );

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

        corsConfiguration.setAllowedOriginPatterns(List.of("*"));
        corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS", "PATCH"));
        corsConfiguration.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type"));
        corsConfiguration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }
}