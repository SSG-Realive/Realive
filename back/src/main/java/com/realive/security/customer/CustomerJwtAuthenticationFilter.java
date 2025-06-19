package com.realive.security.customer;

import com.realive.domain.admin.Admin;
import com.realive.domain.customer.Customer;
import com.realive.security.AdminPrincipal;
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

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (jwtUtil.validateToken(token)) {
                Claims claims = jwtUtil.getClaims(token);
                String subject = claims.getSubject();

                // Customer 토큰인지 확인
                if (JwtUtil.SUBJECT_CUSTOMER.equals(subject)) {
                    String email = claims.get("email", String.class);
                    Long customerId = claims.get("id", Long.class);
                    String role = claims.get("auth", String.class);

                    if (email != null && customerId != null &&role != null) {
                        Customer customerForPrincipal = Customer.builder().id(customerId).email(email).build();
                        CustomerPrincipal customerPrincipal = new CustomerPrincipal(customerForPrincipal);
                        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));
                        Authentication authentication = new UsernamePasswordAuthenticationToken(customerPrincipal, null, authorities);
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
        
        // 요청 경로가 "/api/customer/"로 시작하는 경우에만 이 필터가 동작하도록 합니다.
        // 그 외의 경우(예: /api/public, /api/auth)에는 이 필터를 건너뜁니다.
        log.info("[CustomerJwtFilter] shouldNotFilter 검사. URI: {}", uri);
        boolean shouldNotFilter = !uri.startsWith("/api/customer/");
        log.info("필터 실행 여부 (false여야 실행됨): {}", !shouldNotFilter);
        
        return shouldNotFilter;
    }
    
    
}