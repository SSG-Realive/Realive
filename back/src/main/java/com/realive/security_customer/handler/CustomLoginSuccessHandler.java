package com.realive.security_customer.handler;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;


import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.realive.domain.customer.SignupMethod;
import com.realive.dto.member.MemberLoginDTO;

import java.io.IOException;

//둘 중 어떤 메소드가 동작하는지 확인 
@Log4j2
@Component
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {
    
	//두 메소드 이름 동일 
	
	//파라미터 4개 
	@Override
    public void onAuthenticationSuccess(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain chain,
		Authentication authentication) throws IOException, ServletException {

        log.info("-----success handler 4 -----");

        AuthenticationSuccessHandler.super.onAuthenticationSuccess(request, response, chain, authentication);
    }
	
	//파라미터 3개 
    @Override
    public void onAuthenticationSuccess(
		HttpServletRequest request,
		HttpServletResponse response,
		Authentication authentication) throws IOException, ServletException {

        log.info("-----success handler 3 -----");
		log.info(authentication);

        // //만일 social 회원이라면 회원정보를 수정하는 페이지로 이동
        // MemberLoginDTO loginUser = (MemberLoginDTO) authentication.getPrincipal();
        // log.info("로그인한 사용자 이메일: " + loginUser.getEmail());
        // log.info("SignupMethod: " + loginUser.getSignupMethod());

        // // 임시 회원인지 확인
        // if (loginUser.getSignupMethod() != SignupMethod.USER) {
        //     log.info("임시 회원입니다. 회원정보 수정 페이지로 리다이렉트합니다.");
        //     response.sendRedirect("/customer/update-info");
        // } else {
        //     log.info("정상 회원입니다. 메인 페이지로 리다이렉트합니다.");
        //     response.sendRedirect("/");
        // }

    }
}