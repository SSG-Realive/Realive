package com.realive.security.customer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.realive.domain.customer.Customer;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.Collections;

@Getter
public class CustomerPrincipal implements UserDetails {

    private final Long id;
    private final String email;
    private final String name; // ✅ name 필드 추가!
    @JsonIgnore
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;

    // 생성자에서 Customer 엔티티의 모든 필요한 정보를 담습니다.
    public CustomerPrincipal(Customer customer) {
        this.id = customer.getId();
        this.email = customer.getEmail();
        this.name = customer.getName(); // ✅ name 정보도 함께 저장
        this.password = customer.getPassword();
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
    }

    // --- UserDetails 인터페이스 구현 ---
    @Override
    public String getPassword() { return this.password; }
    @Override
    public String getUsername() { return this.email; }
    // ... isAccountNonExpired() 등 나머지 메서드는 true를 반환 ...
    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}