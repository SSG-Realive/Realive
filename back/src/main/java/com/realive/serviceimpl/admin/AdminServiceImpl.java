package com.realive.serviceimpl.admin;

import com.realive.domain.admin.Admin;
import com.realive.dto.admin.AdminLoginRequestDTO;
import com.realive.dto.admin.AdminLoginResponseDTO;
import com.realive.dto.admin.AdminReadDTO;
import com.realive.dto.admin.AdminRegisterRequestDTO;
import com.realive.exception.UnauthorizedException;
import com.realive.repository.admin.AdminRepository;
import com.realive.security.JwtUtil;
import com.realive.service.admin.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil; // Admin용 토큰 생성 메소드가 있는 JwtUtil 주입

    @Override
    @Transactional
    public Admin register(AdminRegisterRequestDTO dto) {
        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()
                || dto.getPassword() == null || dto.getPassword().isEmpty()
                || dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("이름, 이메일, 비밀번호를 모두 입력해주세요.");
        }

        if (adminRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        Admin admin = Admin.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .build();

        Admin saved = adminRepository.save(admin);
        log.info("관리자 회원가입 완료: id={}, email={}", saved.getId(), saved.getEmail());
        return saved;
    }

    @Override
    @Transactional
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

        log.info("관리자 로그인 시도: email={}, id={}, name={}", admin.getEmail(), admin.getId(), admin.getName());

        // Admin 객체를 위한 토큰 생성 메소드 호출
        String accessToken = jwtUtil.generateAccessToken(admin);
        String refreshToken = jwtUtil.generateRefreshToken(admin);

        //db에 토큰 저장
        admin.setRefreshToken(refreshToken);
        adminRepository.save(admin);

        log.info("생성된 Access Token: {}", accessToken);
        log.info("생성된 Refresh Token: {}", refreshToken);

        return AdminLoginResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .email(admin.getEmail())
                .name(admin.getName())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminLoginResponseDTO getMyInfo(Integer adminId) {
        if (adminId == null) {
            throw new IllegalArgumentException("관리자 ID가 유효하지 않습니다.");
        }
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new UnauthorizedException("사용자를 찾을 수 없습니다."));

        return AdminLoginResponseDTO.builder()
                .accessToken(null) // 마이페이지 조회 시 토큰 재발급 필요 없으면 null
                .refreshToken(null)
                .name(admin.getName())
                .email(admin.getEmail())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AdminReadDTO> findByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("이메일이 유효하지 않습니다.");
        }
        return adminRepository.findByEmail(email)
                .map(this::convertToReadDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Admin> findAdminEntityByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return Optional.empty();
        }
        return adminRepository.findByEmail(email);
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