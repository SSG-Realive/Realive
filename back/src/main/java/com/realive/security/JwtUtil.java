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

    public static final String BEARER_PREFIX = "Bearer ";             // 인증 헤더 접두사
    public static final String SUBJECT_SELLER = "seller";             // 판매자 액세스 토큰 subject
    public static final String SUBJECT_SELLER_REFRESH = "seller_refresh"; // 판매자 리프레시 토큰 subject
    public static final String SUBJECT_ADMIN = "admin";               // 관리자 액세스 토큰 subject
    public static final String SUBJECT_ADMIN_REFRESH = "admin_refresh";   // 관리자 리프레시 토큰 subject

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expiration;

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    /**
     * JWT 토큰 생성
     *
     * @param subject  토큰 주제 (판매자/관리자 구분용)
     * @param id       사용자 식별자
     * @param email    이메일 (액세스 토큰에만 포함)
     * @param name     이름 (액세스 토큰에만 포함)
     * @param duration 토큰 만료 기간 (밀리초)
     * @return 생성된 JWT 토큰 문자열
     */
    private String generateToken(String subject, Long id, String email, String name, long duration) {
        JwtBuilder builder = Jwts.builder()
                .setSubject(subject)
                .claim("id", id)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + duration));

        if (email != null) {
            builder.claim("email", email);
        }
        if (name != null) {
            builder.claim("name", name);
        }

        return builder
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 판매자 access 토큰 생성
    public String generateAccessToken(Seller seller) {
        return generateToken(SUBJECT_SELLER, seller.getId(), seller.getEmail(), seller.getName(), expiration);
    }

    // 판매자 refresh 토큰 생성
    public String generateRefreshToken(Seller seller) {
        long refreshDuration = expiration * 24 * 7;  // 7일간 유효
        return generateToken(SUBJECT_SELLER_REFRESH, seller.getId(), null, null, refreshDuration);
    }

    // 관리자 access 토큰 생성
    public String generateAccessToken(Admin admin) {
        return generateToken(SUBJECT_ADMIN, Long.valueOf(admin.getId()), admin.getEmail(), admin.getName(), expiration);
    }

    // 관리자 refresh 토큰 생성
    public String generateRefreshToken(Admin admin) {
        long refreshDuration = expiration * 24 * 7;  // 7일간 유효
        return generateToken(SUBJECT_ADMIN_REFRESH, Long.valueOf(admin.getId()), null, null, refreshDuration);
    }

    // 토큰 검증
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

    // 토큰에서 claims 추출
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

    // 토큰 만료 여부
    public boolean isTokenExpired(String token) {
        Date expirationDate = getClaims(token).getExpiration();
        return expirationDate.before(new Date());
    }

    // 토큰에서 id 추출
    public Long getUserIdFromToken(String token) {
        return getClaims(token).get("id", Long.class);
    }

    // 토큰에서 admin id 추출
    public Long getAdminIdFromToken(String token) {
        return getClaims(token).get("id", Long.class);
    }

    // 토큰에서 email 추출
    public String getEmailFromToken(String token) {
        return getClaims(token).get("email", String.class);
    }

    // 토큰에서 name 추출
    public String getNameFromToken(String token) {
        return getClaims(token).get("name", String.class);
    }

    // 토큰에서 subject 추출
    public String getSubjectFromToken(String token) {
        return getClaims(token).getSubject();
    }

    // HTTP 요청 헤더에서 토큰 추출
    public String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith(BEARER_PREFIX)) {
            return bearer.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
