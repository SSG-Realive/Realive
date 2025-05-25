package com.realive.service.customer;

import java.util.List;
import java.util.Optional;

import org.hibernate.cache.spi.support.AbstractReadWriteAccess.Item;
import org.springframework.stereotype.Service;

import com.realive.domain.customer.Customer;
import com.realive.domain.customer.Wishlist;
import com.realive.domain.product.Product;
import com.realive.dto.product.ProductListDto;
import com.realive.repository.WishlistRepository;
import com.realive.repository.customer.CustomerRepository;
import com.realive.repository.productview.ProductListRepository;
import com.realive.repository.productview.ProductViewRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Transactional
@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final CustomerService customerService;
    private final ProductViewRepository productViewRepository;
    private final ProductListRepository productListRepository;

    // 찜 토글 (찜 추가/찜 해제)
    public boolean toggleWishlist(Long customerId, Long productId) {
        Optional<Wishlist> wishlistOpt = wishlistRepository.findByCustomerIdAndProductId(customerId, productId);

        if (wishlistOpt.isPresent()) {
            wishlistRepository.delete(wishlistOpt.get()); // 이미 찜되어 있음 → 삭제
            return false; // false = 찜 해제됨
        }

        Customer customer = customerService.getActiveCustomerById(customerId);

        Product product = productViewRepository.findProductById(productId)
                .orElseThrow(() -> new RuntimeException("상품 없음"));

        Wishlist wishlist = Wishlist.builder()
                .customer(customer)
                .product(product)
                .build();

        wishlistRepository.save(wishlist); // 찜 추가
        return true; // true = 찜됨
    }

    //찜 목록 조회
    public List<ProductListDto> getWishlistForCustomer(Long customerId) {
        List<Long> productIds = wishlistRepository.findProductIdsByCustomerId(customerId);
        return productListRepository.getWishlistedProducts(productIds);
    }
 
}
