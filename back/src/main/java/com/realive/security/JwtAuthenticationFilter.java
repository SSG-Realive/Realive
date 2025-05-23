package com.realive.security;

import java.io.IOException;
import java.util.Optional;

import com.realive.domain.admin.Admin;
import com.realive.service.admin.AdminService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final AdminService adminService;

    // 생성자 주입, 순환 참조 방지를 위해 AdminService는 @Lazy로 받음
    public JwtAuthenticationFilter(JwtUtil jwtUtil, @Lazy AdminService adminService) {
        this.jwtUtil = jwtUtil;
        this.adminService = adminService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        log.debug("[AdminJWT] Authorization Header: {}", header);

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            String email = jwtUtil.getEmailFromToken(token);
            log.debug("[AdminJWT] Extracted Email: {}", email);

            if (email != null) {
                Optional<Admin> adminOpt = adminService.findAdminEntityByEmail(email);
                if (adminOpt.isPresent()) {
                    AdminPrincipal principal = new AdminPrincipal(adminOpt.get());
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("[AdminJWT] 인증 객체 설정: {}", authToken);
                } else {
                    log.debug("[AdminJWT] 해당 이메일의 Admin 없음");
                }
            }
        } else {
            log.debug("[AdminJWT] Authorization 헤더 없음 또는 형식 오류");
        }

        filterChain.doFilter(request, response);
    }
}
