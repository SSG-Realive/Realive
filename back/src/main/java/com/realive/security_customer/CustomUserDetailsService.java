package com.realive.security_customer;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.realive.domain.customer.Customer;
import com.realive.dto.member.MemberLoginDTO;
import com.realive.repository.customerlogin.CustomerLoginRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final CustomerLoginRepository customerLoginRepository;

    //사용자 정보를 로드하는 메서드
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        
        log.info("loadUserByUsername: " + username);

        // email 기준으로 Customer 찾기
        Customer customer = customerLoginRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("이메일이 존재하지 않습니다: " + username));

        // MemberLoginDTO로 변환해서 리턴
        return new MemberLoginDTO(
                customer.getEmail(),
                customer.getPassword()
        );

    //     UserDetails sampleUser = User.builder()
    //         .username(username)
    //         //password -> spring security는 password incoder가 있어 에러 날 것
    //         //noop이용해서 에러 잡기 
    //         .password("$2a$10$gfxw06pr1zx02u5Q4EBIruHgRizz9w7xvHGGGVjnWO8CNe18oT.tS") 
    //         .roles("USER")
    //         .build();

    //     return sampleUser;

    // }
    }
    
    
}
