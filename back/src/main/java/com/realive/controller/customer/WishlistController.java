package com.realive.controller.customer;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.realive.dto.customer.wishlist.WishlistDTO;
import com.realive.dto.product.ProductListDTO;
import com.realive.service.customer.CustomerService;
import com.realive.service.customer.WishlistService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

// [Customer] 찜 관련 컨트롤러

@RestController
@RequestMapping("/api/customer/wishlist")
@Log4j2
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;
    private final CustomerService customerService;

    // 찜 토글 (찜 추가/찜 해제)
    @PostMapping("/toggle")
    public ResponseEntity<?> toggleWishlist(Authentication authentication,
                                            @RequestBody WishlistDTO dto) {
        String email = authentication.getName();
        Long customerId = customerService.findIdByEmail(email);
        boolean liked = wishlistService.toggleWishlist(customerId, dto.getProductId());
        return ResponseEntity.ok().body(Map.of("wishlist", liked));
    }

    // 찜 목록 조회
    @GetMapping("/my")
    public ResponseEntity<List<ProductListDTO>> getMyWishlist(Authentication authentication) {
        String email = authentication.getName(); 
        Long customerId = customerService.findIdByEmail(email);
        List<ProductListDTO> wishlists = wishlistService.getWishlistForCustomer(customerId);
        return ResponseEntity.ok(wishlists);
    }
    
}
