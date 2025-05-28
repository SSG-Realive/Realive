package com.realive.service.customer;

import java.util.List;
import java.util.Optional;

import com.realive.dto.wishlist.WishlistMostResponseDTO;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.realive.domain.customer.Customer;
import com.realive.domain.customer.Wishlist;
import com.realive.domain.product.Product;
import com.realive.dto.product.ProductListDTO;
import com.realive.repository.customer.WishlistRepository;
import com.realive.repository.customer.productview.ProductListRepository;
import com.realive.repository.customer.productview.ProductViewRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

// 찜 서비스

@Transactional
@Service
@RequiredArgsConstructor
@Log4j2
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final CustomerService customerService;
    private final ProductViewRepository productViewRepository;
    private final ProductListRepository productListRepository;

    private static final int TOP_N_LIMIT = 20; // 메인 페이지에 표시할 총 인기 찜 상품 개수

    // 찜 토글 (찜 추가/찜 해제)
    public boolean toggleWishlist(Long customerId, Long productId) {
        Optional<Wishlist> wishlistOpt = wishlistRepository.findByCustomerIdAndProductId(customerId, productId);

        if (wishlistOpt.isPresent()) {
            wishlistRepository.delete(wishlistOpt.get()); // 이미 찜되어 있음 → 삭제
            return false; // false = 찜 해제됨
        }

        Customer customer = customerService.getActiveCustomerById(customerId);

        Product product = productViewRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("상품 없음"));

        Wishlist wishlist = Wishlist.builder()
                .customer(customer)
                .product(product)
                .build();

        wishlistRepository.save(wishlist); // 찜 추가
        return true; // true = 찜됨
    }

    //찜 목록 조회
    public List<ProductListDTO> getWishlistForCustomer(Long customerId) {
        List<Long> productIds = wishlistRepository.findProductIdsByCustomerId(customerId);
        return productListRepository.getWishlistedProducts(productIds);
    }

    // 가장 많이 찜된 상품 목록 조회
    public Page<WishlistMostResponseDTO> getMostWishlistedProducts(Pageable pageable) {

        // 데이터베이스 상위 N개 상품을 조회
        List<WishlistMostResponseDTO> topWishlistedProducts =
                wishlistRepository.findTopNWishlistedProducts(TOP_N_LIMIT);

        // 페이징 처리
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), topWishlistedProducts.size());

        // 페이징 범위가 유효한지 확인
        if (start > topWishlistedProducts.size()) {
            return Page.empty(pageable); // 시작 인덱스가 전체 크기보다 크면 빈 페이지 반환
        }
        if (start > end) {
            return Page.empty(pageable);
        }

        List<WishlistMostResponseDTO> pagedList = topWishlistedProducts.subList(start, end);

        // PageImpl 객체를 사용하여 Page<DTO> 형태로 반환
        return new PageImpl<>(pagedList, pageable, topWishlistedProducts.size());
    }
 
}
