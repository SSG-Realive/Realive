package com.realive;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import lombok.extern.log4j.Log4j2;

@SpringBootTest
@Log4j2
public class SecurityTest {
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void testEncode() {

        String encode = passwordEncoder.encode("1111");

        log.info(encode);
    }
}
