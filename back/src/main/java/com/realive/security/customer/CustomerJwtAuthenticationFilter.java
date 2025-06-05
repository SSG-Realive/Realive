package com.realive.security.customer;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.realive.dto.customer.member.MemberLoginDTO;
import com.realive.dto.error.ErrorResponse;

import jakarta.persistence.EntityNotFoundException;
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

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

    try {
            log.info("[CustomerJwtAuthenticationFilter] doFilterInternal 호출, URI: {}", request.getRequestURI());                                
            String token = resolveToken(request);
            log.info("JWT 토큰 추출: {}", token);

            if (token != null && jwtTokenProvider.validateToken(token)) {
                String email = jwtTokenProvider.getUsername(token);
                log.info("토큰에서 추출한 사용자명(email): {}", email);

                MemberLoginDTO memberDTO = (MemberLoginDTO) customUserDetailsService.loadUserByUsername(email);

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(memberDTO, null, memberDTO.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);
                log.info("SecurityContextHolder에 인증 객체 등록 완료");
            }

            filterChain.doFilter(request, response);

        } catch (UsernameNotFoundException | EntityNotFoundException ex) {
            log.error("인증 실패: {}", ex.getMessage());

            // 직접 ErrorResponse 응답
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");

            ErrorResponse error = ErrorResponse.builder()
                    .status(401)
                    .code("UNAUTHORIZED")
                    .message("인증에 실패하였습니다: " + ex.getMessage())
                    .build();

            new ObjectMapper().writeValue(response.getWriter(), error);

        } catch (Exception ex) {
            log.error("JWT 필터 처리 중 알 수 없는 예외 발생", ex);

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json;charset=UTF-8");

            ErrorResponse error = ErrorResponse.builder()
                    .status(500)
                    .code("INTERNAL_SERVER_ERROR")
                    .message("서버 오류가 발생했습니다")
                    .build();

            new ObjectMapper().writeValue(response.getWriter(), error);
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " 이후 토큰만 추출
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