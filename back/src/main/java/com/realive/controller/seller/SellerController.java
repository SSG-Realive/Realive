package com.realive.controller.seller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.realive.domain.seller.Seller;
import com.realive.dto.product.ProductListDto;
import com.realive.dto.seller.SellerLoginRequestDTO;
import com.realive.dto.seller.SellerLoginResponseDTO;
import com.realive.dto.seller.SellerResponseDTO;
import com.realive.dto.seller.SellerSignupDTO;
import com.realive.dto.seller.SellerUpdateDTO;

import com.realive.service.product.ProductService;
import com.realive.service.seller.SellerService;

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
    private final ProductService productService;
    
 // 🔐 로그인 (토큰 발급)
    @PostMapping("/login")
    public ResponseEntity<SellerLoginResponseDTO> login(@RequestBody SellerLoginRequestDTO reqdto) {
        SellerLoginResponseDTO resdto = sellerService.login(reqdto);
        return ResponseEntity.ok(resdto);
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
            @RequestBody @Valid SellerUpdateDTO dto,
            @AuthenticationPrincipal Seller seller) {

        sellerService.updateSeller(seller.getId(), dto);
        return ResponseEntity.ok().build();
    }

    // 🙋‍♀️ 마이페이지 조회 (판매자 정보 + 상품 목록)
    @GetMapping("/me")
    public ResponseEntity<SellerResponseDTO> getMyInfo(@AuthenticationPrincipal Seller seller) {
        Long sellerId = seller.getId();

        List<ProductListDto> products = productService.getProductsBySeller(sellerId);
        SellerResponseDTO resdto = sellerService.getMyInfo(sellerId);
        resdto.setProducts(products);

        return ResponseEntity.ok(resdto);
    }
}
