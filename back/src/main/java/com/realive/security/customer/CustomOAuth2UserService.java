//package com.realive.security.customer;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.log4j.Log4j2;
//import java.util.Optional;
//
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.oauth2.client.registration.ClientRegistration;
//import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
//import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
//import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
//import org.springframework.security.oauth2.core.user.OAuth2User;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import com.realive.domain.customer.Customer;
//import com.realive.domain.customer.SignupMethod;
//import com.realive.dto.member.MemberLoginDTO;
//import com.realive.repository.customer.CustomerRepository;
//
//import java.util.LinkedHashMap;
//import java.util.Map;
//import java.util.Set;
//
////소셜로그인하면 인증 대신 -> 인증한 정보를 사용하기
//@Log4j2
//@Transactional
//@Service
//@RequiredArgsConstructor
//public class CustomOAuth2UserService extends DefaultOAuth2UserService {
//
//    private final CustomerRepository customerRepository;
//    private final PasswordEncoder passwordEncoder;
//
//    @Override
//    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
//
//        log.info("userRequest....");
//        log.info(userRequest);
//
//
//        log.info("oauth2 user.....................................");
//
//
//        //clientName으로 어떤 소셜로그인인지 구분
//        ClientRegistration clientRegistration = userRequest.getClientRegistration();
//        String clientName = clientRegistration.getClientName();
//
//        log.info("NAME: " + clientName);
//
//        OAuth2User oAuth2User = super.loadUser(userRequest);
//        Map<String, Object> paramMap = oAuth2User.getAttributes();
//
//        String email = null;
//
//        switch(clientName){
//            case "Kakao":
//                email = getKakaoEmail(paramMap); //이메일 얻어오기
//                break;
//        }
//
//        log.info("email: " + email);
//
//        // CustomerLoginRepository에서 이메일로 고객 조회. SignupMethod는 USER
//        // 해당 이메일이 없다면, 새로운 Customer 객체를 생성하고 저장
//        // password는 1111로 인코딩해서 임시 비밀번호로 저장해서 사용
//        // SignupMethod값은 kakao또는 google
//
//        // 이메일로 기존 고객 조회
//        Optional<Customer> result = customerRepository.findByEmailIncludingSocial(email);
//
//        Customer customer;
//
//        // 고객이 존재하지 않는 경우-> 임시 회원으로 만들어서 DB에 저장
//        if (result.isEmpty()) {
//
//            log.info("email: " + email);
//            log.info("result.isEmpty(): " + result.isEmpty());
//
//            // 고객이 없다면 신규 생성
//            customer = new Customer(email, passwordEncoder.encode("1111")); // 임시 비밀번호
//
//            // 소셜 로그인 제공자에 따라 signupMethod 설정
//            SignupMethod signupMethod;
//            switch (clientName.toLowerCase()) {
//                case "kakao":
//                    signupMethod = SignupMethod.KAKAO;
//                    break;
//                case "google":
//                    signupMethod = SignupMethod.GOOGLE;
//                    break;
//                default:
//                    signupMethod = SignupMethod.USER; // 기본값 또는 예외 처리
//            }
//
//            customer.setSignupMethod(signupMethod);   // 소셜가입 구분
//            customer.setIsVerified(true);  // 소셜 로그인은 이메일 인증된 것으로 간주
//            customer.setIsActive(true);  // 활성 상태
//            customer.setPenaltyScore(0);  // 초기 페널티 0점
//
//            customerRepository.save(customer);   // 저장
//        } else {
//            customer = result.get();
//        }
//
//
//        // DTO 생성 및 반환
//        // MemberLoginDTO 반환 전에 signupMethod 설정 필요
//        return new MemberLoginDTO(
//                customer.getEmail(),
//                customer.getPassword(),
//                customer.getSignupMethod(), // 이거 추가
//                paramMap
//        );
//
//    }
//    private String getKakaoEmail(Map<String, Object> paramMap){
//
//        log.info("KAKAO-----------------------------------------");
//
//        Object value = paramMap.get("kakao_account");
//
//        log.info(value);
//
//        LinkedHashMap accountMap = (LinkedHashMap) value;
//
//        String email = (String)accountMap.get("email");
//
//        log.info("email..." + email);
//
//        return email;
//    }
//
//}
