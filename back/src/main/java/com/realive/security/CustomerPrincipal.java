//package com.realive.security;
//
//import com.realive.domain.customer.Customer; // 실제 Customer 엔티티 경로
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;
//
//import java.util.Collection;
//import java.util.List; // List 임포트 추가
//
//@RequiredArgsConstructor // final 필드에 대한 생성자 자동 생성
//public class CustomerPrincipal implements UserDetails {
//
//    private final Customer customer; // final로 선언하여 RequiredArgsConstructor가 인식하도록 함
//
//    // UserDetails 인터페이스 메소드 구현
//    @Override
//    public Collection<? extends GrantedAuthority> getAuthorities() {
//        // AdminPrincipal과 유사하게 직접 권한 목록 생성 및 반환
//        return List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
//    }
//
//    @Override
//    public String getPassword() {
//        return customer.getPassword(); // Customer 엔티티의 password 필드 사용
//    }
//
//    @Override
//    public String getUsername() {
//        return customer.getEmail(); // Customer 엔티티의 email 필드를 username으로 사용
//    }
//
//    @Override
//    public boolean isAccountNonExpired() {
//        return true; // 계정 만료 여부 (기본값 true, 필요에 따라 DB 연동)
//    }
//
//    @Override
//    public boolean isAccountNonLocked() {
//        return true; // 계정 잠김 여부 (기본값 true, penaltyScore 활용 가능)
//    }
//
//    @Override
//    public boolean isCredentialsNonExpired() {
//        return true; // 자격 증명 만료 여부 (기본값 true)
//    }
//
//    @Override
//    public boolean isEnabled() {
//        // Customer 엔티티의 isActive 필드와 isVerified 필드를 모두 사용
//        // 둘 다 true여야 활성화된 계정으로 간주 (이 로직은 유지)
//        return customer.getIsActive() && customer.getIsVerified();
//    }
//
//    // Customer 엔티티 객체를 직접 반환하는 getter (AdminPrincipal의 getAdmin()과 유사)
//    public Customer getCustomer() {
//        return this.customer;
//    }
//}
