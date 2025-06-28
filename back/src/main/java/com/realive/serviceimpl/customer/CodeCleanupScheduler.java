package com.realive.serviceimpl.customer;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.realive.repository.verification.VerificationCodeRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CodeCleanupScheduler {

    private final VerificationCodeRepository verificationCodeRepository;

    // 매일 자정에 실행 (cron = "초 분 시 일 월 요일")
    @Scheduled(cron = "0 0 0 * * *")
    public void cleanupExpiredCodes() {
        verificationCodeRepository.deleteByExpiryDateBefore(LocalDateTime.now());
        System.out.println("만료된 인증 코드를 삭제했습니다. 시간: " + LocalDateTime.now());
    }
}



