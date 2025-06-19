package com.realive.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.realive.domain.seller.Seller;
import com.realive.repository.seller.SellerRepository;
import com.realive.security.seller.SellerPrincipal;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SellerJwtAuthenticationFilter extends OncePerRequestFilter {

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

                // Seller 토큰인지 확인
                if (JwtUtil.SUBJECT_SELLER.equals(subject)) {
                    String email = claims.get("email", String.class);
                    Long sellerId = claims.get("id", Long.class);
                    String role = claims.get("auth", String.class);

                    if (email != null && sellerId != null && role != null) {
                        Seller sellerForPrincipal  = Seller.builder().id(sellerId).email(email).build();
                        SellerPrincipal sellerPrincipal = new SellerPrincipal(sellerForPrincipal);
                        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));
                        Authentication authentication = new UsernamePasswordAuthenticationToken(sellerPrincipal, null, authorities);
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
        
        log.info("[SellerJwtFilter] shouldNotFilter 검사. URI: {}", uri);
        boolean shouldNotFilter = !uri.startsWith("/api/seller/");
        log.info("필터 실행 여부 (false여야 실행됨): {}", !shouldNotFilter);
        
        return shouldNotFilter;
    }
}