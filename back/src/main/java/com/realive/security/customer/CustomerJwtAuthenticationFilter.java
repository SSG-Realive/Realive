package com.realive.security.customer;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.realive.dto.common.ApiResponse;
import com.realive.dto.customer.member.MemberLoginDTO;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

// [Customer] JWT 토큰을 이용한 인증 필터

@Component
@RequiredArgsConstructor
@Log4j2
public class CustomerJwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        log.info("[CustomerJwtAuthenticationFilter] doFilterInternal 호출, URI: {}", request.getRequestURI());                                
        String token = resolveToken(request);
        log.info("JWT 토큰 추출: {}", token);

        // /api/customer 경로에 대해서는 반드시 유효한 토큰이 필요
        if (request.getRequestURI().startsWith("/api/customer")) {
            if (token == null || !jwtTokenProvider.validateToken(token)) {
                log.warn("인증이 필요한 요청에 유효한 토큰이 없음 - URI: {}", request.getRequestURI());
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(
                    objectMapper.writeValueAsString(
                        ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "인증이 필요합니다.")
                    )
                );
                return;
            }

            try {
                String email = jwtTokenProvider.getUsername(token);
                log.info("토큰에서 추출한 사용자명(email): {}", email);

                MemberLoginDTO memberDTO = (MemberLoginDTO) customUserDetailsService.loadUserByUsername(email);
                if (memberDTO == null || memberDTO.getId() == null) {
                    log.error("사용자 정보를 찾을 수 없거나 ID가 null입니다.");
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write(
                        objectMapper.writeValueAsString(
                            ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "유효하지 않은 사용자 정보입니다.")
                        )
                    );
                    return;
                }

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(memberDTO, null, memberDTO.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(auth);
                log.info("SecurityContextHolder에 인증 객체 등록 완료 - ID: {}", memberDTO.getId());
            } catch (Exception e) {
                log.error("JWT 인증 처리 중 오류 발생", e);
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(
                    objectMapper.writeValueAsString(
                        ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "인증 처리 중 오류가 발생했습니다.")
                    )
                );
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String uri = request.getRequestURI();
        log.info("shouldNotFilter 호출 - URI: {}", uri);
        boolean result = uri.startsWith("/api/admin") || uri.startsWith("/api/seller") || uri.startsWith("/api/public");
        log.info("필터 제외 여부: {}", result);
        return result;
    }

}