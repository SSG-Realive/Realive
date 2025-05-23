package com.realive.controller.productview;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.realive.dto.member.MemberLoginDTO;
import com.realive.security.JwtResponse;
import com.realive.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;



@RestController
@RequestMapping("/sample")
@Log4j2
@RequiredArgsConstructor
public class SampleController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;


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
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody MemberLoginDTO dto) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword())
        );

        String token = jwtTokenProvider.createToken(authentication);

        return ResponseEntity.ok(new JwtResponse(token));
    }

    

    
}
