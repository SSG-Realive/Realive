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
import lombok.extern.log4j.Log4j2;

/**
 * JwtAuthenticationFilter
 * - 매 요청마다 실행되는 JWT 인증 필터
 * - 요청 헤더에서 JWT 토큰을 추출하고 유효성을 검증
 * - 유효한 경우, 인증 정보를 Spring Security Context에 등록
 */
@Component
@RequiredArgsConstructor
public class SellerJwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil; // JWT 유틸리티 클래스
    private final SellerRepository sellerRepository; // 판매자 정보 조회용 리포지토리

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 1. 요청 헤더에서 Authorization 정보 추출
        String authHeader = request.getHeader("Authorization");

        // 2. "Bearer "로 시작하는 경우에만 처리
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // "Bearer " 제거한 JWT 토큰

            // 3. JWT 유효성 검증
            if (jwtUtil.validateToken(token)) {
                Claims claims = jwtUtil.getClaims(token); // 토큰에서 클레임 추출
                Long sellerId = claims.get("id", Long.class); // seller ID 추출

                // 4. 데이터베이스에서 판매자 조회
                Seller seller = sellerRepository.findById(sellerId)
                        .orElse(null);

                // 5. 판매자가 존재하면 인증 객체 생성 및 등록
                if (seller != null) {
                    List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_SELLER"));
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(seller, null, authorities);

                    SecurityContextHolder.getContext()
                            .setAuthentication(authentication);
                }

            }
        }

        // 6. 다음 필터 체인으로 전달
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/api/seller");
    }
}