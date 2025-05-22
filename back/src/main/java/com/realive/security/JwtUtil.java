package com.realive.security;

import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.realive.domain.seller.Seller;
import com.realive.domain.admin.Admin;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class JwtUtil {

    // ====== 상수 선언 ======
    public static final String BEARER_PREFIX = "Bearer ";             // 인증 헤더 접두사
    public static final String SUBJECT_SELLER = "seller";             // 판매자 액세스 토큰 subject
    public static final String SUBJECT_SELLER_REFRESH = "seller_refresh"; // 판매자 리프레시 토큰 subject
    public static final String SUBJECT_ADMIN = "admin";               // 관리자 액세스 토큰 subject
    public static final String SUBJECT_ADMIN_REFRESH = "admin_refresh";   // 관리자 리프레시 토큰 subject

    // ====== 프로퍼티 주입 ======
    @Value("${jwt.secret}")
    private String secretKey;     // JWT 서명에 사용할 비밀키

    @Value("${jwt.expiration}")
    private long expiration;      // 액세스 토큰 만료 시간(밀리초 단위)

    private Key key;              // 서명 키 객체

    // ====== 초기화 메서드 ======
    @PostConstruct
    public void init() {
        // 프로퍼티로 받은 비밀키를 키 객체로 변환 (HS256 알고리즘용)
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // ====== 토큰 생성 메서드(공통) ======
    /**
     * JWT 토큰 생성
     *
     * @param subject  토큰 주제 (판매자/관리자 구분용)
     * @param id       사용자 식별자
     * @param email    이메일 (액세스 토큰에만 포함)
     * @param duration 토큰 만료 기간 (밀리초)
     * @return 생성된 JWT 토큰 문자열
     */
    private String generateToken(String subject, Long id, String email, long duration) {
        JwtBuilder builder = Jwts.builder()
                .setSubject(subject)
                .claim("id", id)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + duration));

        if (email != null) {
            builder.claim("email", email);
        }

        return builder
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // ====== 판매자 토큰 생성 ======
    /**
     * 판매자 액세스 토큰 생성
     */
    public String generateAccessToken(Seller seller) {
        return generateToken(SUBJECT_SELLER, seller.getId(), seller.getEmail(), expiration);
    }

    /**
     * 판매자 리프레시 토큰 생성
     */
    public String generateRefreshToken(Seller seller) {
        long refreshDuration = expiration * 24 * 7;  // 7일간 유효
        return generateToken(SUBJECT_SELLER_REFRESH, seller.getId(), null, refreshDuration);
    }

    // ====== 관리자 토큰 생성 ======
    /**
     * 관리자 액세스 토큰 생성
     */
    public String generateAccessToken(Admin admin) {
        return generateToken(SUBJECT_ADMIN, Long.valueOf(admin.getId()), admin.getEmail(), expiration);
    }

    /**
     * 관리자 리프레시 토큰 생성
     */
    public String generateRefreshToken(Admin admin) {
        long refreshDuration = expiration * 24 * 7;  // 7일간 유효
        return generateToken(SUBJECT_ADMIN_REFRESH, Long.valueOf(admin.getId()), null, refreshDuration);
    }

    // ====== 토큰 검증 ======
    /**
     * 토큰 유효성 검증
     *
     * @param token JWT 토큰
     * @return 유효하면 true, 그렇지 않으면 false
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // ====== 토큰에서 클레임 정보 추출 ======
    /**
     * 토큰에서 클레임(정보) 추출
     *
     * @param token JWT 토큰
     * @return 토큰 클레임 객체
     */
    public Claims getClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            // 만료된 토큰도 클레임은 꺼낼 수 있음
            return e.getClaims();
        }
    }

    /**
     * 토큰 만료 여부 확인
     *
     * @param token JWT 토큰
     * @return 만료되었으면 true, 아니면 false
     */
    public boolean isTokenExpired(String token) {
        Date expirationDate = getClaims(token).getExpiration();
        return expirationDate.before(new Date());
    }

    /**
     * 토큰에서 사용자 ID 추출
     *
     * @param token JWT 토큰
     * @return 사용자 ID (Long)
     */
    public Long getUserIdFromToken(String token) {
        return getClaims(token).get("id", Long.class);
    }

    // ====== HTTP 요청 헤더에서 토큰 추출 ======
    /**
     * HTTP 요청 헤더 "Authorization"에서 토큰 부분만 추출
     *
     * @param request HttpServletRequest 객체
     * @return 토큰 문자열 (Bearer 접두어 제거), 없으면 null 반환
     */
    public String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith(BEARER_PREFIX)) {
            return bearer.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
