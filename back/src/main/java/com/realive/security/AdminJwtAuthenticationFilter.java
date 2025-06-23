package com.realive.security;

import com.realive.domain.admin.Admin;

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

// [Admin] JWT 토큰을 이용한 인증 필터

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminJwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (jwtUtil.validateToken(token)) {
                Claims claims = jwtUtil.getClaims(token);
                String subject = claims.getSubject();

                // Admin 토큰인지 확인
                if (JwtUtil.SUBJECT_ADMIN.equals(subject)) {
                    String email = claims.get("email", String.class);
                    Long adminId = claims.get("id", Long.class);
                    String role = claims.get("auth", String.class);

                    if (email != null && adminId != null && role != null) {
                        Admin adminForPrincipal = Admin.builder().id(adminId.intValue()).email(email).build();
                        AdminPrincipal adminPrincipal = new AdminPrincipal(adminForPrincipal);
                        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));
                        Authentication authentication = new UsernamePasswordAuthenticationToken(adminPrincipal, null, authorities);
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String uri = request.getRequestURI();
        String method = request.getMethod();

        // 요청 경로가 "/api/admin/"로 시작하는 경우에만 동작
        log.info("[AdminJwtFilter] shouldNotFilter 검사. URI: {}, Method: {}", uri, method);
        
        boolean shouldNotFilter = !uri.startsWith("/api/admin/");
        log.info("필터 실행 여부 (false여야 실행됨): {}", shouldNotFilter);
        log.info("uri.startsWith('/api/admin/'): {}", uri.startsWith("/api/admin/"));

        return shouldNotFilter;
    }
}