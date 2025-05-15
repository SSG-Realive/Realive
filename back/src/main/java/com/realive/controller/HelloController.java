package com.realive.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/test")  // 전체 경로 앞부분
public class HelloController {


    @GetMapping("/hello")      // 전체 최종 경로:
    public String hello(HttpServletRequest request) {
        System.out.println("✅ 요청 URI: " + request.getRequestURI());
        return "Hello, Realive!";

    }
}
