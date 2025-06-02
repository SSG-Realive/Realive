//package com.realive.security;
//
//import com.realive.domain.admin.Admin;
//import com.realive.service.admin.AdminService;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//import java.util.List;
//
//// 관리자용 jwt 인증 필터
//@RequiredArgsConstructor
//public class AdminJwtAuthenticationFilter extends OncePerRequestFilter {
//
//    private final JwtUtil jwtUtil;
//    private final AdminService adminService;
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request,
//                                    HttpServletResponse response,
//                                    FilterChain filterChain)
//            throws ServletException, IOException {
//
//        String authHeader = request.getHeader("Authorization");
//
//        if (authHeader != null && authHeader.startsWith("Bearer ")) {
//            String token = authHeader.substring(7);
//
//            if (jwtUtil.validateToken(token)) {
//                String email = jwtUtil.getEmailFromToken(token);
//
//                // 반드시 Admin 엔티티를 가져와서 AdminPrincipal 생성
//                Admin adminEntity = adminService.findAdminEntityByEmail(email).orElse(null);
//
//                if (adminEntity != null) {
//                    AdminPrincipal adminPrincipal = new AdminPrincipal(adminEntity);
//
//                    UsernamePasswordAuthenticationToken auth =
//                            new UsernamePasswordAuthenticationToken(
//                                    adminPrincipal,
//                                    null,
//                                    List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
//                            );
//                    SecurityContextHolder.getContext().setAuthentication(auth);
//                }
//            }
//        }
//
//        filterChain.doFilter(request, response);
//    }
//
//    @Override
//    protected boolean shouldNotFilter(HttpServletRequest request) {
//        return !request.getRequestURI().startsWith("/api/admin");
//    }
//
//}