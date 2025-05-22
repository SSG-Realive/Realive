package com.realive.controller;

import java.io.IOException;
import java.util.Map;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/kakao")
@Log4j2
@RequiredArgsConstructor
public class KakaoController {

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.redirect-uri}")
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

    // 콜백 처리
    @GetMapping("/callback")
    public ResponseEntity<?> kakaoCallback(@RequestParam String code) throws IOException {

        // 1. 토큰 요청
        String tokenUrl = "https://kauth.kakao.com/oauth/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(params, headers);
        ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(tokenUrl, tokenRequest, Map.class);

        if (!tokenResponse.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("토큰 요청 실패");
        }

        String accessToken = (String) tokenResponse.getBody().get("access_token");

        // 2. 사용자 정보 요청
        HttpHeaders userInfoHeaders = new HttpHeaders();
        userInfoHeaders.setBearerAuth(accessToken);
        HttpEntity<Void> userInfoRequest = new HttpEntity<>(userInfoHeaders);

        ResponseEntity<Map> userInfoResponse = restTemplate.exchange(
            "https://kapi.kakao.com/v2/user/me",
            HttpMethod.GET,
            userInfoRequest,
            Map.class
        );

        if (!userInfoResponse.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("사용자 정보 요청 실패");
        }

        Map<String, Object> kakaoAccount = (Map<String, Object>) userInfoResponse.getBody().get("kakao_account");
        String email = (String) kakaoAccount.get("email");

        log.info("카카오 로그인 사용자 이메일: {}", email);

        // TODO: 이메일로 회원 가입 또는 로그인 처리

        return ResponseEntity.ok(email);
    }
}
