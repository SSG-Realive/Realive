package com.realive.security.seller; // security 패키지 아래에 생성

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.realive.domain.seller.Seller;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.Collections;

@Getter
public class SellerPrincipal implements UserDetails {

    private final Long id;
    private final String email;
    private final String name;
    private final boolean isApproved; // 승인 상태도 포함
    @JsonIgnore
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;

    public SellerPrincipal(Seller seller) {
        this.id = seller.getId();
        this.email = seller.getEmail();
        this.name = seller.getName();
        this.isApproved = seller.isApproved();
        this.password = seller.getPassword();
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_SELLER"));
    }

    // --- UserDetails 인터페이스 구현 ---
    @Override public String getPassword() { return this.password; }
    @Override public String getUsername() { return this.email; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; } // isApproved와 연동할 수도 있음
}