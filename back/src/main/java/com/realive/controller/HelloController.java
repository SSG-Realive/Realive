package com.realive.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import jakarta.servlet.http.HttpServletRequest;


@RestController
@RequestMapping("/test")
public class HelloController {


    @GetMapping("/hello")
    public String hello(HttpServletRequest request) {
        System.out.println("✅ 요청 URI: " + request.getRequestURI());
        return "Hello, Realive!";

    }
}
