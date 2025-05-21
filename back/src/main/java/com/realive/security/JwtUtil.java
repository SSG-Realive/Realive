package com.realive.security;

import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.realive.domain.seller.Seller;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class JwtUtil {
    
    // JWT 비밀키
    @Value("${jwt.secret}")
    private String secretKey;


    // JWT 만료시간
    @Value("${jwt.expiration}")
    private long expiration;

    private Key key;

    // key 초기화
    @PostConstruct
    public void init(){
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // at 생성
    public String generateAccessToken(Seller seller){
        return Jwts.builder()
                .setSubject("seller")
                .claim("id", seller.getId())
                .claim("email", seller.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // rt 생성
    public String generateRefreshToken(Seller seller){
        long refreshExpiration = expiration * 24 * 7;
        return Jwts.builder()
                .setSubject("seller_refresh")
                .claim("id", seller.getId())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    //토큰 검증하는 매서드 
    public boolean validateToken(String token){
        
        try{
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
            return true;

        }catch (Exception e){
            return false;
        }
    }


    //토큰 가져오는 메서드
    public Claims getClaims(String token){
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 헤더값 가져와서 조건 확인후 경우에따라 null 리턴
    public String resolveToken(HttpServletRequest request) {
    String bearer = request.getHeader("Authorization");
    if (bearer != null && bearer.startsWith("Bearer ")) {
        return bearer.substring(7);
    }
    return null;
    }

    // 토큰에서 id 만 가져오는 메서드 
    public Long getUserIdFromToken(String token) {
    Claims claims = getClaims(token);
    return Long.parseLong(claims.getSubject());
    }
}
