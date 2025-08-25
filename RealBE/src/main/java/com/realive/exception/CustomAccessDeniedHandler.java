package com.realive.exception;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private static final Logger log = LoggerFactory.getLogger(CustomAccessDeniedHandler.class);

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        log.warn("권한 부족으로 접근 차단됨 - URI: {}, Method: {}, 메시지: {}",
                request.getRequestURI(),
                request.getMethod(),
                accessDeniedException.getMessage());

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");

        // JSON 응답 바디 직접 작성
        String json = "{\"status\":403,\"code\":\"FORBIDDEN\",\"message\":\"권한이 없습니다. 접근이 거부되었습니다.\"}";
        response.getOutputStream().write(json.getBytes(StandardCharsets.UTF_8));
    }
}
