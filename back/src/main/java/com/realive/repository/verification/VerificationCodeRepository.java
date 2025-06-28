package com.realive.repository.verification;


import org.springframework.data.jpa.repository.JpaRepository;
import com.realive.domain.verification.VerificationCode;
import java.time.LocalDateTime;
import java.util.Optional;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {
    Optional<VerificationCode> findByEmail(String email);
    void deleteByExpiryDateBefore(LocalDateTime now); // 만료일이 특정 시간 이전인 코드를 모두 삭제
}