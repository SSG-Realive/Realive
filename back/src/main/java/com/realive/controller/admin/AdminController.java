package com.realive.controller.admin;

import java.time.Duration;
import com.realive.domain.admin.Admin;
import com.realive.dto.admin.AdminInfoResponseDTO;
import com.realive.dto.admin.AdminLoginRequestDTO;
import com.realive.dto.admin.AdminLoginResponseDTO;
import com.realive.security.AdminPrincipal;
import com.realive.service.admin.AdminService;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

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
    public ResponseEntity<AdminInfoResponseDTO> getMyInfo(@AuthenticationPrincipal AdminPrincipal adminPrincipal) {
        if (adminPrincipal == null) {
            throw new IllegalStateException("관리자 인증이 필요합니다. JWT 토큰을 확인하세요.");
        }
        Admin admin = adminPrincipal.getAdmin();
        AdminInfoResponseDTO dto = new AdminInfoResponseDTO(
                admin.getName(),
                admin.getEmail(),
                "관리자"
        );
        return ResponseEntity.ok(dto);
    }


}