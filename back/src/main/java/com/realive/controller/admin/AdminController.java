package com.realive.controller.admin;

import java.time.Duration;
import com.realive.domain.admin.Admin;
import com.realive.dto.admin.AdminInfoResponseDTO;
import com.realive.dto.admin.AdminLoginRequestDTO;
import com.realive.dto.admin.AdminLoginResponseDTO;
import com.realive.dto.admin.AdminRegisterRequestDTO;
import com.realive.security.AdminPrincipal;
import com.realive.security.JwtUtil;
import com.realive.service.admin.AdminService;
import com.realive.service.auth.LogoutService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final AdminService adminService;
    private final JwtUtil jwtUtil;
    private final LogoutService logoutService;
    // 관리자 회원가입
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody AdminRegisterRequestDTO dto) {
        adminService.register(dto);
        return ResponseEntity.ok("관리자 회원가입 완료");
    }
    
    @PostMapping("logout")
    public ResponseEntity<Void> adminLogout(@AuthenticationPrincipal AdminPrincipal principal) {
        if (principal != null) {
            log.info("관리자 로그아웃 요청: ID {}", principal.getId());
            logoutService.adminLogout(principal.getId());
        }
        return ResponseEntity.ok().build();
    }


    //  관리자 로그인
    @PostMapping("/login")
public ResponseEntity<AdminLoginResponseDTO> login(
        @RequestBody @Valid AdminLoginRequestDTO reqDTO) {

    // 1) 로그인 + 토큰 쌍 발급
    AdminLoginResponseDTO resDTO = adminService.login(reqDTO);
    // └ resDTO : { accessToken, refreshToken, email, name }

    // 2) 별도 쿠키 설정 없음  ❌  (프런트에서 localStorage 에 저장하도록 통일)
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