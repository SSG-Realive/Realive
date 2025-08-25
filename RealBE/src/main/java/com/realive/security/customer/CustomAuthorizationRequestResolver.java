package com.realive.security.customer;

import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

import lombok.extern.log4j.Log4j2;

// [Customer] 스프링의 기본 OAuth2 인증 요청 처리를 커스터마이징

@Log4j2
@Component
public class CustomAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final OAuth2AuthorizationRequestResolver defaultResolver;

    public CustomAuthorizationRequestResolver(ClientRegistrationRepository repo) {
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(repo, "/oauth2/authorization");
    }

    // 일반 OAuth2 인증 요청 처리
    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        return customize(defaultResolver.resolve(request), request);
    }

    // 특정 클라이언트 등록 ID에 대한 OAuth2 인증 요청 처리 (예: Google, Kakao 등)
    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
            return customize(defaultResolver.resolve(request, clientRegistrationId), request);
        }

        private OAuth2AuthorizationRequest customize(OAuth2AuthorizationRequest request, HttpServletRequest httpRequest) {
        if (request == null) return null;

        String redirectTo = httpRequest.getParameter("redirectTo");
        log.info("받은 redirectTo 파라미터: " + redirectTo);
        
        if (redirectTo != null) {
            // 세션에 저장
            httpRequest.getSession().setAttribute("redirectTo", redirectTo);
            log.info("세션에 redirectTo 저장 완료");
            
            // 저장 확인
            String saved = (String) httpRequest.getSession().getAttribute("redirectTo");
            log.info("저장 확인: " + saved);
        } else {
            log.info("redirectTo 파라미터가 null입니다");
        }

        return request;
    }

}
