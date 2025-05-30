    package com.realive.config;

    import com.realive.security.SellerJwtAuthenticationFilter;
    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;

    import org.springframework.context.annotation.Configuration;
    import org.springframework.core.annotation.Order;
    import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
    import org.springframework.security.config.annotation.web.builders.HttpSecurity;
    import org.springframework.security.config.http.SessionCreationPolicy;
    import org.springframework.security.web.SecurityFilterChain;
    import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
    import org.springframework.context.annotation.Bean;
    import org.springframework.web.cors.CorsConfigurationSource;

    @EnableMethodSecurity(prePostEnabled = true)
    @Configuration
    @RequiredArgsConstructor
    @Slf4j
    @Order(2)
    public class SellerSecurityConfig {

        private final SellerJwtAuthenticationFilter sellerJwtAuthenticationFilter;

        // ✅ SecurityConfig 에서 이미 등록된 공통 Bean 주입받음
        private final CorsConfigurationSource corsConfigurationSource;

        @Bean
        public SecurityFilterChain sellerFilterChain(HttpSecurity http) throws Exception {
            log.info("✅ SellerSecurityConfig 적용");

            http
                    .securityMatcher("/api/seller/**")
                    .cors(cors -> cors.configurationSource(corsConfigurationSource))  // 주입된 Bean 사용
                    .csrf(csrf -> csrf.disable())
                    .formLogin(form -> form.disable())
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .addFilterBefore(sellerJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/api/seller/login").permitAll()
                            .requestMatchers("/api/seller/**").authenticated()
                    );


            return http.build();
        }
    }
