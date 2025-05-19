package com.realive.controller.seller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.realive.dto.seller.SellerLoginRequestDTO;
import com.realive.dto.seller.SellerLoginResponseDTO;
import com.realive.security.JwtUtil;
import com.realive.service.seller.SellerService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PostMapping;


@RestController
@RequestMapping("/api/seller")
@RequiredArgsConstructor
public class SellerController {

    private final SellerService sellerService;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<SellerLoginResponseDTO> login(@RequestBody SellerLoginRequestDTO reqdto){
        SellerLoginResponseDTO resdto = sellerService.login(reqdto);
        return ResponseEntity.ok(resdto);
    }
    
}
