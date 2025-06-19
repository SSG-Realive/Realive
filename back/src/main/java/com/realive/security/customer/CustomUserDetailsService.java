package com.realive.security.customer;

import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.realive.domain.customer.Customer;
import com.realive.dto.customer.member.MemberLoginDTO;
import com.realive.repository.customer.CustomerRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

// [Customer] UserDetailsService

@Service("customUserDetailsService")
@Log4j2
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final CustomerRepository customerRepository;

    //사용자 정보를 로드하는 메서드
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("loadUserByUsername: " + username);

        // email 기준으로 Customer 찾기
        Customer customer = customerRepository.findByEmailIncludingSocial(username)
                .orElseThrow(() -> new UsernameNotFoundException("이메일이 존재하지 않습니다: " + username));

        // ✅ DTO가 아닌, Spring Security의 User 객체를 생성하여 반환합니다.
        // User 생성자: User(사용자 이름, 비밀번호, 권한 목록)
        return new CustomerPrincipal(customer);
    }
}