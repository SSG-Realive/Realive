package com.realive.serviceimpl.auth; // 실제 서비스 구현체 패키지 경로

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.realive.domain.customer.Customer;
import com.realive.domain.customer.SignupMethod;
import com.realive.domain.verification.VerificationCode;
import com.realive.dto.auth.TokenResponseDTO;
import com.realive.dto.customer.login.CustomerLoginRequestDTO;
import com.realive.dto.customer.member.MemberJoinDTO;
import com.realive.repository.customer.CustomerRepository;
import com.realive.security.JwtUtil;
import com.realive.service.auth.AuthService;
import com.realive.service.auth.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    // ✅ 인증에 필요한 모든 의존성을 여기서 주입받습니다.
    private final CustomerRepository userRepository;
    private final com.realive.repository.verification.VerificationCodeRepository verificationCodeRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;

    @Override
    public void sendVerificationCode(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }
        
        String verificationCode = generateRandomCode();
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(5); // 5분 후 만료

        // 이미 해당 이메일로 요청한 기록이 있다면 코드와 만료시간만 업데이트
        Optional<VerificationCode> existingCode = verificationCodeRepository.findByEmail(email);
        if (existingCode.isPresent()) {
            existingCode.get().updateCode(verificationCode, expiryDate);
        } else {
            verificationCodeRepository.save(new VerificationCode(email, verificationCode, expiryDate));
        }
        
        String subject = "[Realive] 이메일 인증 코드입니다.";
        String text = "Realive 서비스 가입을 위한 인증 코드입니다.\n\n인증 코드: " + verificationCode + "\n\n이 코드는 5분 후에 만료됩니다.";
        emailService.sendEmail(email, subject, text);
    }

    @Override
    public void signUpWithVerification(MemberJoinDTO memberJoinDTO) {
        VerificationCode verificationCode = verificationCodeRepository.findByEmail(memberJoinDTO.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("인증 코드가 발송된 적이 없는 이메일입니다."));
        
        if (verificationCode.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("인증 코드가 만료되었습니다.");
        }
        // 사용자가 입력한 인증코드는 MemberJoinDTO에 추가되어 있어야 합니다.
        if (!verificationCode.getCode().equals(memberJoinDTO.getVerificationCode())) {
            throw new IllegalArgumentException("인증 코드가 일치하지 않습니다.");
        }

        // 인증 성공, 사용자 생성
        Customer user = Customer.builder()
                .email(memberJoinDTO.getEmail())
                .password(passwordEncoder.encode(memberJoinDTO.getPassword()))
                .name(memberJoinDTO.getName())
                .phone(memberJoinDTO.getPhone())
                .address(memberJoinDTO.getAddress())
                .birth(memberJoinDTO.getBirth())
                .gender(memberJoinDTO.getGender())
                .signupMethod(SignupMethod.USER)
                .isVerified(true) // ✅ 이메일 인증 완료
                .isActive(true) // ✅ 회원가입 시 기본적으로 활성화 상태
                .build();
        userRepository.save(user);

        // 인증에 사용된 코드는 즉시 삭제
        verificationCodeRepository.delete(verificationCode);
    }

    // --- 스케줄러: 만료된 인증 코드 주기적 삭제 ---
    
    @Scheduled(cron = "0 0 0 * * *")
    public void cleanupExpiredCodes() {
        verificationCodeRepository.deleteByExpiryDateBefore(LocalDateTime.now());
        log.info("만료된 인증 코드 {}건 삭제");
    }

    // 6자리 숫자 랜덤 코드 생성 헬퍼 메소드
    private String generateRandomCode() {
        return String.valueOf((int) (Math.random() * 900000) + 100000);
    }
}