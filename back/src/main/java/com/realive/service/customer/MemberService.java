package com.realive.service.customer;


import java.time.LocalDate;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.realive.domain.customer.Customer;
import com.realive.domain.customer.SignupMethod;
import com.realive.dto.member.MemberJoinDTO;
import com.realive.dto.member.MemberReadDTO;
import com.realive.repository.customerlogin.CustomerLoginRepository;
import com.realive.security.JwtTokenProvider;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Transactional
@Service
@RequiredArgsConstructor
public class MemberService {

    private final CustomerLoginRepository customerLoginRepository;
    private final PasswordEncoder passwordEncoder;  // 빈 주입
    private final JwtTokenProvider jwtTokenProvider;


    // 회원가입: 소셜로그인(임시회원정보 수정)
    public void updateTemporaryUserInfo(MemberJoinDTO request, String authenticatedEmail) {
        // 인증된 사용자 이메일로 회원 조회
        Customer customer = customerLoginRepository.findByEmailIncludingSocial(authenticatedEmail)
                .orElseThrow(() -> new RuntimeException("회원 정보를 찾을 수 없습니다."));

        // 회원정보 업데이트
        customer.setName(request.getName());
        customer.setPhone(request.getPhone()); // DTO 필드명 맞춤
        customer.setAddress(request.getAddress());
        customer.setBirth(request.getBirth());
        customer.setGender(request.getGender());
        

        // 비밀번호 변경 필요시 암호화 후 변경
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            String encodedPassword = passwordEncoder.encode(request.getPassword());
            customer.setPassword(encodedPassword);
        }

        // 임시회원 → 일반회원 상태 변경
        customer.setSignupMethod(SignupMethod.USER);

        customerLoginRepository.save(customer);
    }

    // 일반회원가입
    public String register(MemberJoinDTO dto) {
        // 1) 이미 USER로 가입된 이메일인지 체크
        if (customerLoginRepository.findByEmailIncludingSocial(dto.getEmail()).isPresent()
                && customerLoginRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        // 2) 엔티티 생성
        Customer customer = new Customer();
        customer.setEmail(dto.getEmail());
        customer.setPassword(passwordEncoder.encode(dto.getPassword()));
        customer.setName(dto.getName());
        customer.setPhone(dto.getPhone());
        customer.setAddress(dto.getAddress());
        customer.setBirth(dto.getBirth());
        customer.setGender(dto.getGender());

        customer.setSignupMethod(SignupMethod.USER);
        customer.setIsVerified(false);   // 일반 회원은 이메일 인증 로직이 있을 경우 false
        customer.setIsActive(true);
        customer.setPenaltyScore(0);

        customerLoginRepository.save(customer);

        // 이메일로 바로 토큰 발급 (Authentication 불필요)
        return jwtTokenProvider.generateToken(dto.getEmail());
    }

    // 회원정보 조회
    public MemberReadDTO getMyProfile(String email) {
        Customer customer = customerLoginRepository
            .findByEmailIncludingSocial(email) // 소셜·일반 모두 조회 가능
            .orElseThrow(() -> new RuntimeException("회원 정보를 찾을 수 없습니다."));

        MemberReadDTO dto = new MemberReadDTO();
        dto.setEmail(customer.getEmail());
        dto.setName(customer.getName());
        dto.setPhone(customer.getPhone());
        dto.setAddress(customer.getAddress());
        dto.setBirth(customer.getBirth());
        dto.setCreated(customer.getCreated()); 
        return dto;
    }

    public void updateMember(String email, MemberReadDTO dto) {
        Customer customer = customerLoginRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다"));

        customer.setName(dto.getName());
        customer.setPhone(dto.getPhone());
        customer.setAddress(dto.getAddress());
        customer.setBirth(dto.getBirth());  

        customerLoginRepository.save(customer); // JPA 더티 체킹으로 자동 반영
    }

    public void deactivateByEmail(String email) {
    Customer customer = customerLoginRepository.findByEmailIncludingSocial(email)
            .orElseThrow(() -> new RuntimeException("회원이 존재하지 않습니다."));

    customer.deactivate();
    // 별도의 save() 호출 없어도 @Transactional 내에서 변경 감지되어 반영됨
}

    
}
