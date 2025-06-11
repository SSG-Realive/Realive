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

@Slf4j
@Component
@RequiredArgsConstructor
public class SellerJwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil; // JWT 유틸
    private final SellerRepository sellerRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        log.info("[SellerJwtAuthenticationFilter] doFilterInternal 호출, URI: {}", request.getRequestURI());
        String authHeader = request.getHeader("Authorization");
        log.info("Authorization 헤더: {}", authHeader);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            log.info("추출한 토큰: {}", token);

            if (jwtUtil.validateToken(token)) {
                Claims claims = jwtUtil.getClaims(token);
                String email = claims.get("email", String.class); // ★ 이메일 추출

                Seller seller = sellerRepository.findByEmail(email)
                        .orElseThrow(() -> new IllegalArgumentException("판매자 정보를 찾을 수 없습니다."));

                log.info("DB에서 조회한 Seller id: {}, email: {}", seller.getId(), seller.getEmail());

                List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_SELLER"));

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(seller,
                        null, authorities);

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("SecurityContextHolder 에 Seller 등록 완료");
            } else {
                log.warn("JWT token 검증 실패");
            }
        } else {
            log.warn("Authorization 헤더가 없거나 Bearer 형식이 아님");
        }

        filterChain.doFilter(request, response);
    }

    // 체인에서 이미 /api/seller/** 로 한정했으므로 필터는 항상 작동
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return false;
    }
}
