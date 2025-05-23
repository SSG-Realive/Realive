package com.realive.dto.member;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.realive.domain.customer.SignupMethod;

import java.util.*;

@Getter
@Setter
public class MemberLoginDTO implements UserDetails, OAuth2User {

    private String email;
    @ToString.Exclude
    private String password;
    private SignupMethod signupMethod; 

    // OAuth2 attributes (nullable)
    private Map<String, Object> attributes;

    public MemberLoginDTO() {
    }
    

    // 일반 로그인 시 사용되는 생성자
    public MemberLoginDTO(String email, String password) {
        this.email = email;
        this.password = password;
        this.attributes = new HashMap<>();
        this.signupMethod = SignupMethod.USER; // USER로 지정
    }

    public MemberLoginDTO(String email, String password, SignupMethod signupMethod, Map<String, Object> attributes) {
        this.email = email;
        this.password = password;
        this.signupMethod = signupMethod;
        this.attributes = attributes;
    }


    // 권한 설정 (기본 사용자 권한)
    //GrantedAuthority의 하위 타입만 들어갈 수 있음
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    // 사용자명 반환
    @Override
    public String getUsername() {
        return this.email;
    }

    // OAuth2 속성 반환
    @Override
    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

    // OAuth2 name (일반적으로 이메일이나 id)
    @Override
    public String getName() {
        return this.email;
    }

    // 계정 만료 여부
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // 계정 잠김 여부
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // 자격 증명 만료 여부
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // 계정 활성화 여부
    @Override
    public boolean isEnabled() {
        return true;
    }
}
