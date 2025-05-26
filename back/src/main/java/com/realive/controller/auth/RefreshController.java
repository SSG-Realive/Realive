package com.realive.controller.auth;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.realive.domain.seller.Seller;
import com.realive.dto.seller.SellerLoginResponseDTO;
import com.realive.exception.UnauthorizedException;
import com.realive.repository.seller.SellerRepository;
import com.realive.security.JwtUtil;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
/**
 * RefreshController
 * - 클라이언트가 전달한 Refresh Token으로 새로운 Access Token을 발급해주는 컨트롤러
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class RefreshController {

    private final JwtUtil jwtUtil;
    private final SellerRepository sellerRepository;

    /**
     * Refresh Token으로 새로운 Access Token 발급
     *
     * @param request HTTP 요청 (쿠키에서 refreshToken을 추출)
     * @return 새로운 Access Token과 사용자 정보가 담긴 응답 DTO
     */
    @PostMapping("/refresh")
    public ResponseEntity<SellerLoginResponseDTO> refreshAcessToken(HttpServletRequest request) {
        // 쿠키에서 refreshToken 추출
        String refreshToken = extractRefreshTokenFromCookie(request);

        // 토큰 유효성 검증
        if (refreshToken == null || !jwtUtil.validateToken(refreshToken)) {
            throw new UnauthorizedException("리프레시 토큰이 유효하지 않습니다.");
        }

        // 토큰에서 사용자 ID 추출
        Long sellerId = jwtUtil.getUserIdFromToken(refreshToken);

        // 사용자 조회
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new UnauthorizedException("판매자 정보를 찾을 수 없습니다"));

        // 새로운 Access Token 생성
        String newAccessToken = jwtUtil.generateAccessToken(seller);

        // 응답 DTO 생성
        SellerLoginResponseDTO dto = SellerLoginResponseDTO.builder()
                .accessToken(newAccessToken)
                .email(seller.getEmail())
                .name(seller.getName())
                .build();

        return ResponseEntity.ok(dto);
    }

    /**
     * HTTP 요청의 쿠키에서 refreshToken을 추출
     *
     * @param request HTTP 요청
     * @return refreshToken 문자열 (없으면 null 반환)
     */
    private String extractRefreshTokenFromCookie(HttpServletRequest request){
        if (request.getCookies() == null) return null;

        for(Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals("refreshToken")) {
                return cookie.getValue();
            }
        }
        return null;
    }
}

