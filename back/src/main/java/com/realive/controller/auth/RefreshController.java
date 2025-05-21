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
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class RefreshController {
    
    private final JwtUtil jwtUtil;
    private final SellerRepository sellerRepository;

    @PostMapping("/refresh")
    public ResponseEntity<SellerLoginResponseDTO> refreshAcessToken(HttpServletRequest request) {
       
        String refreshToken = extractRefreshTokenFromCookie(request);
        if (refreshToken == null || !jwtUtil.validateToken(refreshToken)) {
            throw new UnauthorizedException("리프레시 토큰이 유효하지 않습니다.");    
        }

        Long sellerId = jwtUtil.getUserIdFromToken(refreshToken);

        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new UnauthorizedException("판매자 정보를 찾을 수 없습니다"));
        
        String newAccessToken = jwtUtil.generateAccessToken(seller);

        SellerLoginResponseDTO dto = SellerLoginResponseDTO.builder()
                .accessToken(newAccessToken)
                .email(seller.getEmail())
                .name(seller.getName())
                .build();
        
        return ResponseEntity.ok(dto);
    }

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
