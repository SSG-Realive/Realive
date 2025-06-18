package com.realive.security.customer;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.extern.log4j.Log4j2;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.realive.domain.customer.SignupMethod;
import com.realive.dto.customer.member.MemberLoginDTO;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

// [Customer] 로그인 성공 핸들러

@Log4j2
@Component
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {
     
    private final JwtTokenProvider jwtTokenProvider;

    // 생성자 주입
    public CustomLoginSuccessHandler(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public void onAuthenticationSuccess(
		HttpServletRequest request,
		HttpServletResponse response,
		Authentication authentication) throws IOException, ServletException {

        log.info("-----success handler 3 -----");
		log.info(authentication);

        // 만일 social 임시회원이라면 회원정보를 수정하는 페이지로 이동
        MemberLoginDTO loginUser = (MemberLoginDTO) authentication.getPrincipal();
        log.info("로그인한 사용자 이메일: " + loginUser.getEmail());
        log.info("SignupMethod: " + loginUser.getSignupMethod());

        // 임시 회원 여부 판단
        boolean isTemporary = loginUser.getSignupMethod() != SignupMethod.USER;

        String token = jwtTokenProvider.createToken(authentication);
        log.info("토큰 생성 완료: " + token.substring(0, 20) + "...");

        // 세션에서 원래 redirectTo (콜백 URL) 가져오기
        String callbackUrl = (String) request.getSession().getAttribute("redirectTo");
        log.info("세션에서 가져온 callbackUrl: " + callbackUrl);
        request.getSession().removeAttribute("redirectTo");

        if (callbackUrl == null) {
            callbackUrl = "/customer/oauth/callback"; // 기본 콜백 URL
            log.info("기본 콜백 URL 사용: " + callbackUrl);
        }

        try {
            // 토큰과 사용자 정보를 쿼리 파라미터로 추가
            String redirectUrl = callbackUrl + 
                "?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8) +
                "&email=" + URLEncoder.encode(loginUser.getEmail(), StandardCharsets.UTF_8) +
                "&temporaryUser=" + isTemporary;

            log.info("리다이렉트 URL: " + redirectUrl);
            response.sendRedirect(redirectUrl);
            
        } catch (Exception e) {
            log.error("리다이렉트 처리 중 오류 발생", e);
            response.sendRedirect("/login?error=redirect_failed");
        }

    }
    
}