package com.realive.security.customer;

import com.realive.domain.customer.Customer;
import com.realive.security.JwtUtil;
import com.realive.service.customer.CustomerService;
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
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerJwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomerService customerService; // ✅ 이메일로 Customer 조회를 위해 필요

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

                boolean isTokenValid = jwtUtil.validateToken(token);
                log.info("토큰 검증 결과: {}", isTokenValid);

                if (isTokenValid) {
                    Claims claims = jwtUtil.getClaims(token);

                    String subject = claims.getSubject();
                    String userType = claims.get("userType", String.class);
                    String email = claims.get("email", String.class);
                    String role = claims.get("auth", String.class);

                    log.info("토큰 Subject: {}", subject);
                    log.info("사용자 타입: {}", userType);
                    log.info("추출된 이메일: {}", email);
                    log.info("추출된 권한: {}", role);

                    boolean isCustomerToken = "customer".equals(userType)
                            || (userType == null && ("customer".equals(subject) || isEmailFormat(subject)));

                    if (isCustomerToken) {
                        String userEmail = email != null ? email : subject;

                        if (userEmail != null && role != null) {
                            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));

                            // ✅ CustomerPrincipal 사용을 위한 Customer 조회
                            Customer customer = customerService.getByEmail(userEmail);
                            CustomerPrincipal principal = new CustomerPrincipal(customer);
                            Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null, authorities);
                            SecurityContextHolder.getContext().setAuthentication(authentication);

                            log.info("SecurityContext 설정 완료 - 사용자: {}, 권한: {}", userEmail, role);
                        } else {
                            log.warn("이메일 또는 권한 정보 누락 - email: {}, subject: {}, role: {}", email, subject, role);
                        }
                    } else {
                        log.warn("Customer 토큰이 아님 - Subject: {}, userType: {}", subject, userType);
                    }
                } else {
                    log.warn("토큰 검증 실패");
                }
            } else {
                log.warn("Authorization 헤더 없음 또는 형식 오류: {}", authHeader);
            }
        } catch (Exception e) {
            log.error("JWT 필터 처리 중 예외 발생: {}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
        log.info("=== CustomerJwtFilter doFilterInternal 완료 ===");
    }

    private boolean isEmailFormat(String text) {
        return text != null && text.contains("@") && text.contains(".");
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String uri = request.getRequestURI();
        boolean shouldNotFilter = !uri.startsWith("/api/customer/");
        log.info("[CustomerJwtFilter] shouldNotFilter 검사. URI: {}, 결과: {}", uri, shouldNotFilter ? "건너뜀" : "실행");
        return shouldNotFilter;
    }
}