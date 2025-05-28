package com.realive.controller.seller;

import java.time.Duration;


import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.realive.domain.seller.Seller;
import com.realive.dto.seller.SellerLoginRequestDTO;
import com.realive.dto.seller.SellerLoginResponseDTO;
import com.realive.dto.seller.SellerResponseDTO;
import com.realive.dto.seller.SellerSignupDTO;
import com.realive.dto.seller.SellerUpdateDTO;
import com.realive.security.JwtUtil;
import com.realive.service.seller.SellerService;

import jakarta.servlet.http.HttpServletResponse;
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
   
    
 // 🔐 로그인 (토큰 발급)
    @PostMapping("/login")
    public ResponseEntity<SellerLoginResponseDTO> login(@RequestBody SellerLoginRequestDTO reqdto, HttpServletResponse response) {
        SellerLoginResponseDTO resdto = sellerService.login(reqdto);
       
        Seller seller = sellerService.getByEmail(reqdto.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(seller);

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("none")
                .path("/")
                .maxAge(Duration.ofDays(7))
                .build();
        
        response.setHeader("Set-Cookie", refreshCookie.toString());
        
        return ResponseEntity.ok(resdto);
    }
    //로그아웃(토큰덮어쓰기)
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        
        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken","")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(0)
                .build();
        
        response.setHeader("Set-Cookie", deleteCookie.toString());
        
        return ResponseEntity.noContent().build();
    }
    

    // 📝 회원가입
    @PostMapping("/signup")
    public ResponseEntity<Void> signup(
            @RequestPart @Valid SellerSignupDTO dto,
            @RequestPart MultipartFile businessLicense,
            @RequestPart MultipartFile bankAccountCopy) {

        sellerService.registerSeller(dto, businessLicense, bankAccountCopy);
        return ResponseEntity.ok().build();
    }

    // 🔄 판매자 정보 수정
    @PutMapping("/me")
    public ResponseEntity<Void> updateSeller(
            @RequestBody @Valid SellerUpdateDTO dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        sellerService.updateSeller(email, dto);
        return ResponseEntity.ok().build();
    }

    // 🙋‍♀️ 마이페이지 조회 (판매자 정보 )
    @GetMapping("/me")
    public ResponseEntity<SellerResponseDTO> getMyInfo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        SellerResponseDTO dto = sellerService.getMyInfo(email);
        return ResponseEntity.ok(dto);
    }
}
