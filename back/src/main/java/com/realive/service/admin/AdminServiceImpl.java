package com.realive.service.admin;

import com.realive.domain.admin.Admin;
import com.realive.dto.admin.AdminLoginRequestDTO;
import com.realive.dto.admin.AdminLoginResponseDTO;
import com.realive.dto.admin.AdminReadDTO;
import com.realive.repository.admin.AdminRepository;
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
    // private final JwtTokenProvider jwtTokenProvider; // 실제 JWT 사용 시 필요

    @Override
    @Transactional(readOnly = true)
    public AdminLoginResponseDTO login(AdminLoginRequestDTO loginRequestDto) {
        Admin admin = adminRepository.findByEmail(loginRequestDto.getEmail())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다.")); // 간단한 예외

        if (!passwordEncoder.matches(loginRequestDto.getPassword(), admin.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다."); // 간단한 예외
        }

        // 실제 JWT 토큰 생성 로직 필요
        String accessToken = "generated-access-token"; // 임시 값
        String refreshToken = "generated-refresh-token"; // 임시 값

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
        return adminRepository.findById(adminId)
                .map(this::convertToReadDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AdminReadDTO> getAdminByEmail(String email) {
        return adminRepository.findByEmail(email)
                .map(this::convertToReadDTO);
    }

    private AdminReadDTO convertToReadDTO(Admin admin) {
        return AdminReadDTO.builder()
                .id(admin.getId())
                .email(admin.getEmail())
                .name(admin.getName())
                // .authorities(admin.getRole() != null ? admin.getRole().toString() : null) // 예시
                .createdAt(admin.getCreatedAt())
                .build();
    }
}
