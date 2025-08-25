package com.realive.service.auth;

import com.realive.domain.auth.PasswordResetToken;
import com.realive.domain.customer.Customer;
import com.realive.repository.auth.PasswordResetTokenRepository;
import com.realive.repository.customer.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * 비밀번호 재설정(Password Reset) 관련 비즈니스 로직을 수행하는 서비스.
 *
 * <p>• 호출 순서:
 *   1. requestReset(email)
 *   2. verifyCode(email, code)
 *   3. confirmReset(email, newPassword, confirmPassword)
 * </p>
 */
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepo;
    private final CustomerRepository          userRepo;
    private final EmailService                emailService;
    private final PasswordEncoder             passwordEncoder;

    /** 인증 코드 만료 시간 (분) */
    private static final int EXPIRE_MINUTES = 10;

    /**
     * 6자리 난수 인증 코드를 생성한다.
     *
     * @return 000000 ~ 999999 형식의 문자열
     */
    private String generateCode() {
        return String.format("%06d",
            new SecureRandom().nextInt(1_000_000)
        );
    }

    /**
     * 비밀번호 재설정 요청: 기존 토큰 삭제 → 새 코드 발급 → 이메일 전송.
     *
     * @param email 재설정 요청할 사용자 이메일
     * @throws IllegalArgumentException 이메일이 가입된 사용자가 아닐 경우
     */
    @Transactional
    public void requestReset(String email) {
        // 1) 사용자 존재 확인
        if (userRepo.findByEmail(email).isEmpty()) {
            throw new IllegalArgumentException("가입된 이메일이 아닙니다.");
        }

        // 2) 기존 토큰이 있으면 삭제
        tokenRepo.deleteById(email);

        // 3) 새 인증 코드 생성 및 만료시간 설정
        String code = generateCode();
        LocalDateTime expireAt = LocalDateTime.now().plusMinutes(EXPIRE_MINUTES);

        PasswordResetToken token = new PasswordResetToken();
        token.setEmail(email);
        token.setCode(code);
        token.setExpireAt(expireAt);
        tokenRepo.save(token);

        // 4) 인증 이메일 발송
        String subject = "Realive 비밀번호 재설정 인증번호";
        String text = String.format(
            "안녕하세요,\n\n비밀번호 재설정을 요청하셨습니다.\n" +
            "인증번호: %s\n(유효기간: %d분)\n\n감사합니다.",
            code, EXPIRE_MINUTES
        );
        emailService.sendEmail(email, subject, text);
    }

    /**
     * 사용자로부터 받은 인증 코드의 유효성을 검사한다.
     *
     * @param email 인증 대상 이메일
     * @param code  사용자가 입력한 인증 코드
     * @throws IllegalArgumentException
     *   - 토큰이 존재하지 않거나 코드가 일치하지 않을 때,
     *   - 토큰이 만료되었을 때
     */
    @Transactional(readOnly = true)
    public void verifyCode(String email, String code) {
        PasswordResetToken token = tokenRepo
            .findByEmailAndCode(email, code)
            .orElseThrow(() ->
                new IllegalArgumentException("인증번호가 일치하지 않습니다.")
            );

        if (token.getExpireAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("인증번호가 만료되었습니다.");
        }
        // 검증 성공 후 별도 처리 불필요
    }

    /**
     * 인증이 완료된 상태에서 실제로 비밀번호를 변경한다.
     *
     * @param email            비밀번호를 변경할 사용자 이메일
     * @param newPassword      새 비밀번호
     * @param confirmPassword  확인용 비밀번호 (newPassword 와 일치해야 함)
     * @throws IllegalArgumentException
     *   - 새 비밀번호와 확인용 비밀번호가 다를 때
     *   - 해당 이메일에 대한 인증 토큰이 존재하지 않거나 만료되었을 때
     */
    @Transactional
    public void confirmReset(
            String email,
            String newPassword,
            String confirmPassword
    ) {
        // 1) 비밀번호 일치 여부 확인
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("비밀번호 확인이 일치하지 않습니다.");
        }

        // 2) 토큰 존재 및 만료 검사
        PasswordResetToken token = tokenRepo.findById(email)
            .orElseThrow(() ->
                new IllegalArgumentException("인증이 필요합니다.")
            );
        if (token.getExpireAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("인증번호가 만료되었습니다.");
        }

        // 3) 회원 엔티티 조회
        Customer user = userRepo.findByEmail(email)
            .orElseThrow(() ->
                new IllegalArgumentException("존재하지 않는 사용자입니다.")
            );

        // 4) 비밀번호 암호화 후 업데이트
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(user);

        // 5) 사용된 토큰 삭제
        tokenRepo.delete(token);
    }
}
