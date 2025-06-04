package com.realive.controller.admin;

import java.time.Duration;
import com.realive.domain.admin.Admin;
import com.realive.dto.admin.AdminInfoResponseDTO;
import com.realive.dto.admin.AdminLoginRequestDTO;
import com.realive.dto.admin.AdminLoginResponseDTO;
import com.realive.security.AdminPrincipal;
import com.realive.security.JwtUtil;
import com.realive.service.admin.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final AdminService adminService;
    private final JwtUtil jwtUtil;

    //  관리자 로그인
    @PostMapping("/login")
    public ResponseEntity<AdminLoginResponseDTO> login(
            @RequestBody AdminLoginRequestDTO reqDTO,
            HttpServletResponse response) {

        // 1. 로그인 및 토큰 발급 (서비스에서 처리)
        AdminLoginResponseDTO resDTO = adminService.login(reqDTO);

        // 2. Refresh Token을 HttpOnly 쿠키로 설정 (서비스에서 내려준 refreshToken 활용)
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", resDTO.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("none")
                .path("/")
                .maxAge(Duration.ofDays(7))
                .build();

        response.setHeader("Set-Cookie", refreshCookie.toString());

        // 3. accessToken은 필요한 경우 body로 전달
        return ResponseEntity.ok(resDTO);
    }

    // 관리자 정보 조회
    @GetMapping("/me")
    public ResponseEntity<AdminInfoResponseDTO> getMyInfo() {
        log.info("관리자 정보 조회 요청");
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("현재 인증 정보: {}", authentication);
        
        if (authentication == null || !(authentication.getPrincipal() instanceof AdminPrincipal)) {
            log.warn("인증 정보가 없거나 AdminPrincipal이 아님");
            throw new IllegalStateException("관리자 인증이 필요합니다. JWT 토큰을 확인하세요.");
        }

        AdminPrincipal adminPrincipal = (AdminPrincipal) authentication.getPrincipal();
        Admin admin = adminPrincipal.getAdmin();
        
        log.info("조회된 관리자 정보: {}", admin);
        
        AdminInfoResponseDTO dto = new AdminInfoResponseDTO(
                admin.getName(),
                admin.getEmail(),
                "관리자"
        );
        return ResponseEntity.ok(dto);
    }
}