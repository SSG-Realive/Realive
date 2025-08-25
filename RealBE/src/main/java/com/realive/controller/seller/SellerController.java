package com.realive.controller.seller;

import java.time.Duration;

import com.realive.security.seller.SellerPrincipal;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import com.realive.event.FileUploadEvnetPublisher;
import com.realive.repository.seller.SellerRepository;
import com.realive.security.JwtUtil;
import com.realive.security.seller.SellerPrincipal;
import com.realive.service.auth.LogoutService;
import com.realive.service.seller.SellerService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;

@Slf4j
@RestController
@RequestMapping("/api/seller")
@RequiredArgsConstructor
public class SellerController {

    private final SellerService sellerService;
    private final JwtUtil jwtUtil;
    private final FileUploadEvnetPublisher fileUploadEvnetPublisher;
    private final SellerRepository sellerRepository;
    private final LogoutService logoutService;


    // 🔐 로그인 (토큰 발급)
    @PostMapping("/login")
    public ResponseEntity<SellerLoginResponseDTO> login(@RequestBody @Valid SellerLoginRequestDTO request) {
        // 컨트롤러는 이제 서비스의 login 메서드를 호출하고 결과만 받습니다.
        SellerLoginResponseDTO responseDto = sellerService.login(request);
        return ResponseEntity.ok(responseDto);
    }

    // 로그아웃 (토큰 덮어쓰기)
    @PostMapping("/logout")
    public ResponseEntity<Void> sellerLogout(@AuthenticationPrincipal SellerPrincipal principal) {
        if (principal != null) {
            log.info("판매자 로그아웃 요청: ID {}", principal.getId());
            logoutService.sellerLogout(principal.getId());
        }
        return ResponseEntity.ok().build();
    }

    // 📝 회원가입
    @PostMapping("/signup")
    public ResponseEntity<Void> signup(
            @RequestPart @Valid SellerSignupDTO dto,
            @RequestPart MultipartFile businessLicense,
            @RequestPart MultipartFile bankAccountCopy) {

        Seller savedSeller = sellerService.registerSeller(dto);

        fileUploadEvnetPublisher.publish(savedSeller, businessLicense, bankAccountCopy);
        return ResponseEntity.ok().build();
    }

    //판매자 정보 보기
    @GetMapping("/me")
    public ResponseEntity<SellerResponseDTO> getMyInfo(@AuthenticationPrincipal SellerPrincipal principal) {
        Long sellerId = principal.getId();
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new EntityNotFoundException("Seller not found with id: " + sellerId));

        log.info("Seller email from @AuthenticationPrincipal: {}", seller.getEmail());

        SellerResponseDTO dto = sellerService.getMyInfo(seller);
        return ResponseEntity.ok(dto);
    }

    // 🔄 판매자 정보 수정 - @AuthenticationPrincipal 사용
    @PutMapping("/me")
    public ResponseEntity<Void> updateSeller(
            @AuthenticationPrincipal SellerPrincipal principal, // 파라미터로 주입
            @RequestBody @Valid SellerUpdateDTO dto) {

        Long sellerId = principal.getId();
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new EntityNotFoundException("Seller not found with id: " + sellerId));
    

        sellerService.updateSeller(seller, dto);
        sellerRepository.save(seller);  // ← 이 줄 추가!
        return ResponseEntity.ok().build();
    }
}
