package com.realive.admin;

import com.realive.domain.admin.Admin;
import com.realive.repository.admin.AdminRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootTest
public class AdminSignupTest {

    @Autowired
    private AdminRepository adminRepository;

    @Test
    public void insertAdmin() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        Admin admin = new Admin();
        admin.setEmail("admin@admin.com");
        admin.setName("Gudo");
        admin.setPassword(encoder.encode("admin")); // 패스워드 암호화

        adminRepository.save(admin);

        System.out.println("관리자 데이터 저장 완료");
    }
}