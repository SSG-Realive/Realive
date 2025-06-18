package com.realive.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.realive.domain.seller.Seller;
import com.realive.repository.seller.SellerRepository;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// [Seller] JWT 토큰을 이용한 인증 필터

@Slf4j
@Component
@RequiredArgsConstructor
public class SellerJwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final SellerRepository sellerRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        log.info("[SellerJwtAuthenticationFilter] doFilterInternal 호출, URI: {}", request.getRequestURI());
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (jwtUtil.validateToken(token)) {
                // 토큰에서 클레임 추출 및 이메일 확인
                Claims claims = jwtUtil.getClaims(token);
                String email = claims.get("email", String.class);

                // 이메일로 DB에서 판매자 조회
                Seller seller = sellerRepository.findByEmail(email)
                        .orElseThrow(() -> new IllegalArgumentException("판매자 정보를 찾을 수 없습니다."));

                // 권한 부여 및 SecurityContext 설정
                List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_SELLER"));
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        seller, null, authorities);

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                log.warn("JWT token 검증 실패");
            }
        }

        // 필터 체인 계속 진행
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/seller");
    }
}