package com.realive.dto.customer.member;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.realive.domain.customer.SignupMethod;

import jakarta.validation.constraints.NotBlank;

import java.util.*;

// [Customer] 로그인DTO

public class MemberLoginDTO implements UserDetails, OAuth2User {

    @NotBlank(message = "이메일을 입력해주세요.")
    private String email;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    @ToString.Exclude
    private String password;

    private SignupMethod signupMethod;

    private Map<String, Object> attributes;

    public MemberLoginDTO() {
    }

    public MemberLoginDTO(String email, String password) {
        this.email = email;
        this.password = password;
        this.attributes = new HashMap<>();
        this.signupMethod = SignupMethod.USER;
    }

    public MemberLoginDTO(String email, String password, SignupMethod signupMethod, Map<String, Object> attributes) {
        this.email = email;
        this.password = password;
        this.signupMethod = signupMethod;
        this.attributes = attributes;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public SignupMethod getSignupMethod() {
        return signupMethod;
    }

    public void setSignupMethod(SignupMethod signupMethod) {
        this.signupMethod = signupMethod;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    // --- 인터페이스 구현부 ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

    @Override
    public String getName() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
