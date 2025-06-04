package com.realive.security;

import com.realive.domain.admin.Admin;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

// 관리자용 jwt 인증 필터
@Component
@RequiredArgsConstructor
@Slf4j
public class AdminJwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        log.info("Authorization 헤더: {}", authHeader);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            log.info("추출된 토큰: {}", token);

            if (jwtUtil.validateToken(token)) {
                log.info("토큰 유효성 검증 성공");
                
                // 토큰의 subject가 admin인 경우에만 처리
                String subject = jwtUtil.getSubjectFromToken(token);
                log.info("토큰 subject: {}", subject);
                
                if (JwtUtil.SUBJECT_ADMIN.equals(subject)) {
                    log.info("관리자 토큰 확인됨");
                    
                    String email = jwtUtil.getEmailFromToken(token);
                    Long adminId = jwtUtil.getAdminIdFromToken(token);
                    String name = jwtUtil.getNameFromToken(token);
                    
                    log.info("토큰에서 추출한 정보 - email: {}, adminId: {}, name: {}", email, adminId, name);

                    if (email != null && adminId != null) {
                        Admin admin = Admin.builder()
                                .id(adminId.intValue())
                                .email(email)
                                .name(name != null ? name : "관리자")
                                .build();
                        
                        log.info("생성된 Admin 객체: {}", admin);
                        
                        AdminPrincipal adminPrincipal = new AdminPrincipal(admin);
                        log.info("생성된 AdminPrincipal: {}", adminPrincipal);

                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(
                                        adminPrincipal,
                                        null,
                                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                                );
                        SecurityContextHolder.getContext().setAuthentication(auth);
                        log.info("SecurityContext에 인증 정보 설정 완료");
                    } else {
                        log.warn("토큰에서 필수 정보(email, adminId)를 추출할 수 없음");
                    }
                } else {
                    log.warn("관리자 토큰이 아님. subject: {}", subject);
                }
            } else {
                log.warn("토큰 유효성 검증 실패");
            }
        } else {
            log.debug("Authorization 헤더가 없거나 Bearer 형식이 아님");
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/api/admin");
    }

}