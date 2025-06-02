//package com.realive.security.customer;
//
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.extern.log4j.Log4j2;
//
//
//import org.springframework.security.core.Authentication;
//import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
//import org.springframework.stereotype.Component;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.realive.domain.customer.SignupMethod;
//import com.realive.dto.member.MemberLoginDTO;
//
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.Map;
//
//
//@Log4j2
//@Component
//public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {
//
//    private final ObjectMapper objectMapper = new ObjectMapper();
//    private final JwtTokenProvider jwtTokenProvider;
//
//    // 생성자 주입
//    public CustomLoginSuccessHandler(JwtTokenProvider jwtTokenProvider) {
//        this.jwtTokenProvider = jwtTokenProvider;
//    }
//
//    @Override
//    public void onAuthenticationSuccess(
//		HttpServletRequest request,
//		HttpServletResponse response,
//		Authentication authentication) throws IOException, ServletException {
//
//        log.info("-----success handler 3 -----");
//		log.info(authentication);
//
//        //만일 social 임시회원이라면 회원정보를 수정하는 페이지로 이동
//        MemberLoginDTO loginUser = (MemberLoginDTO) authentication.getPrincipal();
//        log.info("로그인한 사용자 이메일: " + loginUser.getEmail());
//        log.info("SignupMethod: " + loginUser.getSignupMethod());
//
//        // 임시 회원 여부 판단
//        boolean isTemporary = loginUser.getSignupMethod() != SignupMethod.USER;
//
//        // JwtTokenProvider로 토큰 생성
//        String token = jwtTokenProvider.createToken(authentication);
//
//        Map<String, Object> responseData = new HashMap<>();
//        responseData.put("email", loginUser.getEmail());
//        responseData.put("temporaryUser", isTemporary);
//        responseData.put("token", token);
//
//        response.setContentType("application/json;charset=UTF-8");
//        response.setStatus(HttpServletResponse.SC_OK);
//        objectMapper.writeValue(response.getWriter(), responseData);
//    }
//
//
//}