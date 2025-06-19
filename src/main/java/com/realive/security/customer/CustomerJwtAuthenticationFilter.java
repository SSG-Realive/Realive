package com.realive.security.customer;

import com.realive.security.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerJwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        log.info("=== CustomerJwtFilter doFilterInternal 시작 ===");
        log.info("URI: {}", request.getRequestURI());

        try {
            String authHeader = request.getHeader("Authorization");
            log.info("Authorization 헤더: {}", authHeader != null ? "있음" : "없음");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                log.info("토큰 추출 성공 - 토큰 길이: {}", token.length());
                log.info("토큰 앞 20자: {}", token.substring(0, Math.min(token.length(), 20)));

                log.info("토큰 검증 시작...");
                boolean isTokenValid = jwtUtil.validateToken(token);
                log.info("토큰 검증 결과: {}", isTokenValid);

                if (isTokenValid) {
                    log.info("토큰 유효함 - Claims 추출 시작");
                    Claims claims = jwtUtil.getClaims(token);
                    log.info("Claims 추출 완료");

                    String subject = claims.getSubject(); // 이메일 또는 사용자 ID
                    String userType = claims.get("userType", String.class); // 사용자 타입 claim
                    String email = claims.get("email", String.class);
                    String role = claims.get("auth", String.class);

                    log.info("토큰 Subject: {}", subject);
                    log.info("사용자 타입: {}", userType);
                    log.info("추출된 이메일: {}", email);
                    log.info("추출된 권한: {}", role);

                    // Customer 토큰인지 확인
                    // 1. userType이 "customer"인 경우
                    // 2. userType이 없고 subject가 "customer"인 경우 (기존 토큰)
                    // 3. userType이 없고 subject가 이메일 형식인 경우 (다른 기존 토큰)
                    boolean isCustomerToken = "customer".equals(userType) ||
                            (userType == null && ("customer".equals(subject) || isEmailFormat(subject)));

                    log.info("Customer 토큰 여부: {}", isCustomerToken);

                    if (isCustomerToken) {
                        log.info("Customer 토큰 확인됨 - Authentication 생성");

                        // 이메일 결정: email claim이 있으면 사용, 없으면 subject 사용
                        String userEmail = email != null ? email : subject;

                        if (userEmail != null && role != null) {
                            log.info("사용자 정보 유효함 - Authentication 생성");
                            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));
                            Authentication authentication = new UsernamePasswordAuthenticationToken(userEmail, null, authorities);
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            log.info("SecurityContext 설정 완료 - 사용자: {}, 권한: {}", userEmail, role);
                        } else {
                            log.warn("사용자 이메일 또는 권한이 null임 - email: {}, subject: {}, role: {}",
                                    email, subject, role);
                        }
                    } else {
                        log.warn("Customer 토큰이 아님 - Subject: {}, userType: {}", subject, userType);
                    }
                } else {
                    log.warn("토큰 검증 실패");
                }
            } else {
                if (authHeader == null) {
                    log.warn("Authorization 헤더가 없음");
                } else {
                    log.warn("Authorization 헤더 형식 오류 - Bearer로 시작하지 않음: {}", authHeader);
                }
            }
        } catch (Exception e) {
            log.error("JWT 필터 처리 중 예외 발생: {}", e.getMessage(), e);
        }

        log.info("다음 필터로 진행...");
        filterChain.doFilter(request, response);
        log.info("=== CustomerJwtFilter doFilterInternal 완료 ===");
    }

    /**
     * 이메일 형식인지 확인하는 헬퍼 메소드
     */
    private boolean isEmailFormat(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        // 간단한 이메일 형식 확인 (@ 포함 여부)
        return text.contains("@") && text.contains(".");
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String uri = request.getRequestURI();

        log.info("[CustomerJwtFilter] shouldNotFilter 검사. URI: {}", uri);
        boolean shouldNotFilter = !uri.startsWith("/api/customer/");

        log.info("shouldNotFilter 반환값: {} (true=필터건너뛰기, false=필터실행)", shouldNotFilter);
        log.info("결과: 이 요청은 JWT 필터를 {}합니다", shouldNotFilter ? "건너뛰" : "실행");

        return shouldNotFilter;
    }
}