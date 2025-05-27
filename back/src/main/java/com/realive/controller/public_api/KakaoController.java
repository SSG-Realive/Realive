package com.realive.controller.public_api;

import java.io.IOException;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

// Kakao 로그인 컨트롤러

@RestController
@RequestMapping("/api/public/kakao")
@Log4j2
@RequiredArgsConstructor
public class KakaoController {

    @Value("${KAKAO_CLIENT_ID}")
    private String clientId;

    @Value("${KAKAO_REDIRECT_URI}")
    private String redirectUri;

    private final RestTemplate restTemplate = new RestTemplate();

    // 인가코드 요청
    @GetMapping("/login")
    public void redirectToKakao(HttpServletResponse response) throws IOException {
        String kakaoAuthUrl = "https://kauth.kakao.com/oauth/authorize?" +
            "response_type=code" +
            "&client_id=" + clientId +
            "&redirect_uri=" + redirectUri;

        response.sendRedirect(kakaoAuthUrl);
    }

}
