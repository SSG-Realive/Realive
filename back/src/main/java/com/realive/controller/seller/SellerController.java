package com.realive.controller.seller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.realive.dto.seller.SellerLoginRequestDTO;
import com.realive.dto.seller.SellerLoginResponseDTO;
import com.realive.dto.seller.SellerResponseDTO;
import com.realive.dto.seller.SellerSignupDTO;
import com.realive.dto.seller.SellerUpdateDTO;
import com.realive.security.JwtUtil;
import com.realive.service.seller.SellerService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;






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

    @PostMapping("/signup")
    public ResponseEntity<Void> signup(
        @RequestPart @Valid SellerSignupDTO dto, 
        @RequestPart MultipartFile businessLicense,
        @RequestPart MultipartFile bankAccountCopy) {
        
        sellerService.registerSeller(dto, businessLicense, bankAccountCopy);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/me")
    public ResponseEntity<Void> updateSeller(
                @RequestBody @Valid SellerUpdateDTO dto,
                HttpServletRequest request){
    
        String token = jwtUtil.resolveToken(request);
         Long sellerId = jwtUtil.getUserIdFromToken(token);
    
        sellerService.updateSeller(sellerId, dto);
        return ResponseEntity.ok().build();
    
    }
    

    @GetMapping("/me")
    public ResponseEntity<SellerResponseDTO> getMyInfo(HttpServletRequest request) {
        String token = jwtUtil.resolveToken(request);
        Long sellerId = jwtUtil.getUserIdFromToken(token);

        SellerResponseDTO resdto = sellerService.getMyInfo(sellerId);
        
        return ResponseEntity.ok(resdto);
    }
    
    
}
