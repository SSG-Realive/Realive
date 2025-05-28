package com.realive.controller.customer;

import java.util.List;
import java.util.Map;

import com.realive.dto.wishlist.WishlistMostResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.realive.dto.product.ProductListDTO;
import com.realive.dto.wishlist.WishlistDTO;
import com.realive.service.customer.CustomerService;
import com.realive.service.customer.WishlistService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

// 찜 컨트롤러

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

    // 찜 목록에 가장 많이 추가된 물품 표시
    @GetMapping("/most-popular")
    public ResponseEntity<Page<WishlistMostResponseDTO>> getMostPopularWishlists(
            @PageableDefault(size = 5, sort = "wishCounts", direction = Sort.Direction.DESC) Pageable pageable) {
        try{

            Page<WishlistMostResponseDTO> mostWishlistedProducts =
                    wishlistService.getMostWishlistedProducts(pageable);

            return ResponseEntity.ok(mostWishlistedProducts);
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // 서버 오류

        }
    }
}
