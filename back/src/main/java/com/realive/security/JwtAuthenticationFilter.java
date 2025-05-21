package com.realive.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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

/**
 * JWT 인증 필터
 * - 모든 HTTP 요청마다 실행되어 Authorization 헤더의 JWT 토큰을 검사하고,
 *   유효한 경우 인증 객체를 SecurityContext에 등록한다.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // JWT 유틸리티 클래스 (토큰 검증 및 파싱)
    private final JwtUtil jwtUtil;

    // 판매자 정보 조회용 Repository
    private final SellerRepository sellerRepository;

    /**
     * 필터 실행 메서드 - 요청이 올 때마다 한 번만 실행됨
     *
     * @param request HTTP 요청
     * @param response HTTP 응답
     * @param filterChain 필터 체인
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Authorization 헤더에서 JWT 추출
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // "Bearer " 제거

            // 토큰 유효성 검증
            if (jwtUtil.validateToken(token)) {
                // 토큰에서 Claims(정보) 추출
                Claims claims = jwtUtil.getClaims(token);

                // Claims에서 사용자 ID 추출
                Long sellerId = claims.get("id", Long.class);

                // DB에서 해당 판매자 조회
                Seller seller = sellerRepository.findById(sellerId)
                        .orElse(null);

                // 판매자 존재 시 SecurityContext에 인증 정보 설정
                if (seller != null) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    seller, null, null); // 권한(Authorities) 없음
                    SecurityContextHolder.getContext()
                            .setAuthentication(authentication);
                }
            }
        }

        // 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }
}
