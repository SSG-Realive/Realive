package com.realive.login;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Rollback;

import com.realive.domain.customer.Customer;
import com.realive.domain.customer.Gender;
import com.realive.domain.customer.SignupMethod;
import com.realive.repository.customer.CustomerRepository;

import jakarta.transaction.Transactional;
//import lombok.extern.log4j.Log4j2;

@Transactional
@SpringBootTest
//@Log4j2
public class LoginTests {

    @Autowired
    private CustomerRepository customerRepository;

    //PasswordEncoder 주입받기 
    @Autowired
    private PasswordEncoder passwordEncoder;

    // @Rollback
    // @Test
    // public void testEncode() {

    //     for (int i = 1; i <= 20; i++) {
    //         Customer customer = new Customer("user" + i + "@gmail.com", passwordEncoder.encode("1111"));
    //         customer.setName("사용자" + i);
    //         customer.setPhone("010-1234-56" + String.format("%02d", i));
    //         customer.setAddress("서울시 어딘가 " + i + "번지");
    //         customer.setBirth(LocalDate.of(1990 + (i % 10), (i % 12) + 1, (i % 28) + 1));
    //         customer.setGender(i % 2 == 0 ? Gender.MALE : Gender.FEMALE);

    //         // SignupMethod는 USER / KAKAO / GOOGLE 순환으로 분배
    //         if (i % 3 == 0) {
    //             customer.setSignupMethod(SignupMethod.KAKAO);
    //         } else if (i % 3 == 1) {
    //             customer.setSignupMethod(SignupMethod.GOOGLE);
    //         } else {
    //             customer.setSignupMethod(SignupMethod.USER);
    //         }

    //         // isActive와 penaltyScore 설정
    //         if (i % 5 == 0) {
    //             customer.setPenaltyScore(15); // 제재 점수
    //             customer.setIsActive(false);  // 탈퇴 상태
    //         }

    //         customerRepository.save(customer);
    //     }

    // }

    
}
