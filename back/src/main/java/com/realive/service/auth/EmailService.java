package com.realive.service.auth; // 실제 서비스 패키지 경로에 맞게 수정해주세요.

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    // ✅ Spring Boot Mail 의존성을 통해 주입받는 메일 발송기
    private final JavaMailSender mailSender;

    /**
     * 지정된 이메일 주소로 메일을 발송합니다.
     * @param to      받는 사람 이메일 주소
     * @param subject 메일 제목
     * @param text    메일 본문
     */
    public void sendEmail(String to, String subject, String text) {
        // 1. 간단한 텍스트 이메일을 위한 객체 생성
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("jungchanho3887@gamil.com"); // 발신자 이메일 주소 (실제 발신자 이메일로 변경 필요)

        // 2. 메일 정보 설정
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        
        // 3. 메일 발송
        mailSender.send(message);
    }
}