package com.realive.service.admin;

import com.realive.domain.admin.Admin;
import com.realive.dto.admin.AdminLoginRequestDTO;
import com.realive.dto.admin.AdminLoginResponseDTO;
import com.realive.dto.admin.AdminReadDTO;
import com.realive.exception.UnauthorizedException;
import com.realive.repository.admin.AdminRepository;
import com.realive.security.JwtUtil; // Admin용 메소드가 포함된 JwtUtil 또는 Admin 전용 JwtUtil
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil; // Admin용 토큰 생성 메소드가 있는 JwtUtil 주입

    @Override
    @Transactional(readOnly = true)
    public AdminLoginResponseDTO login(AdminLoginRequestDTO loginRequestDto) {
        if (loginRequestDto.getEmail() == null || loginRequestDto.getEmail().trim().isEmpty() ||
                loginRequestDto.getPassword() == null || loginRequestDto.getPassword().isEmpty()) {
            throw new IllegalArgumentException("이메일과 비밀번호를 모두 입력해주세요.");
        }

        Admin admin = adminRepository.findByEmail(loginRequestDto.getEmail())
                .orElseThrow(() -> new UnauthorizedException("사용자를 찾을 수 없거나 인증 정보가 잘못되었습니다."));

        if (!passwordEncoder.matches(loginRequestDto.getPassword(), admin.getPassword())) {
            throw new UnauthorizedException("비밀번호가 일치하지 않습니다.");
        }

        // Admin 객체를 위한 토큰 생성 메소드 호출
        String accessToken = jwtUtil.generateAccessToken(admin);
        String refreshToken = jwtUtil.generateRefreshToken(admin);

        return AdminLoginResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .name(admin.getName())
                .message("로그인 성공")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AdminReadDTO> getAdminById(Integer adminId) {
        if (adminId == null) {
            throw new IllegalArgumentException("관리자 ID가 유효하지 않습니다.");
        }
        return adminRepository.findById(adminId)
                .map(this::convertToReadDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AdminReadDTO> getAdminByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("이메일이 유효하지 않습니다.");
        }
        return adminRepository.findByEmail(email)
                .map(this::convertToReadDTO);
    }

    private AdminReadDTO convertToReadDTO(Admin admin) {
        return AdminReadDTO.builder()
                .id(admin.getId())
                .email(admin.getEmail())
                .name(admin.getName())
                .createdAt(admin.getCreatedAt())
                .build();
    }
}
