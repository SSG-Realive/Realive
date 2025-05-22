package com.realive.controller.productview;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.realive.dto.member.MemberLoginDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RestController
@RequestMapping("/sample")
@Log4j2
@RequiredArgsConstructor
public class SampleController {

    @PreAuthorize("permitAll()")
    @GetMapping("/ex1")
    public void ex1() {
        log.info("ex1");
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/ex2")
    public void ex2(@AuthenticationPrincipal MemberLoginDTO dto) {
        log.info("ex2");
        log.info(dto);
    }
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/ex3")
    public void ex3() {
        log.info("ex3");
    }
    
    
}
