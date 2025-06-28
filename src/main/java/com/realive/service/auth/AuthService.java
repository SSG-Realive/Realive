package com.realive.service.auth;

import com.realive.dto.customer.member.MemberJoinDTO; // 회원가입용 DTO
import com.realive.dto.customer.login.CustomerLoginRequestDTO; // ✅ 방금 확인한 로그인 요청 DTO
import com.realive.dto.auth.TokenResponseDTO; // 토큰 응답 DTO

/**
 * 인증 관련 비즈니스 로직을 처리하는 서비스 인터페이스
 */
public interface AuthService {

    /**
     * 사용자가 입력한 이메일로 인증 코드를 발송합니다.
     * @param email 인증 코드를 받을 사용자의 이메일 주소
     */
    void sendVerificationCode(String email);

    /**
     * 이메일 인증 코드를 검증하고 최종적으로 회원가입을 처리합니다.
     * @param memberJoinDTO 사용자가 입력한 회원가입 정보 (인증코드 포함)
     */
    void signUpWithVerification(MemberJoinDTO memberJoinDTO);

    
}