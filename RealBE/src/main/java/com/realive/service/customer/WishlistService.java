package com.realive.service.customer;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.realive.domain.common.enums.MediaType;
import com.realive.domain.product.ProductImage;
import com.realive.repository.product.ProductImageRepository;
import org.springframework.stereotype.Service;

import com.realive.domain.customer.Customer;
import com.realive.domain.customer.Wishlist;
import com.realive.domain.product.Product;
import com.realive.dto.product.ProductListDTO;
import com.realive.repository.customer.WishlistRepository;
import com.realive.repository.customer.productview.ProductListRepository;
import com.realive.repository.customer.productview.ProductViewRepository;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

// [Customer] 찜 Service

@Transactional
@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final CustomerService customerService;
    private final ProductViewRepository productViewRepository;
    private final ProductListRepository productListRepository;
    private final ProductImageRepository productImageRepository;

    // 찜 토글 (찜 추가/찜 해제)
    public boolean toggleWishlist(Long customerId, Long productId) {
        
        Optional<Wishlist> wishlistOpt = wishlistRepository.findByCustomerIdAndProductId(customerId, productId);

        if (wishlistOpt.isPresent()) {
            wishlistRepository.delete(wishlistOpt.get()); // 이미 찜되어 있음 → 삭제
            return false; // false = 찜 해제됨
        }

        Customer customer = customerService.getActiveCustomerById(customerId);

        Product product = productViewRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("상품이 존재하지 않습니다. id=" + productId));

        Wishlist wishlist = Wishlist.builder()
                .customer(customer)
                .product(product)
                .build();

        wishlistRepository.save(wishlist); // 찜 추가
        return true; // true = 찜됨
    }

    //찜 목록 조회
    @Transactional(readOnly = true)
    public List<ProductListDTO> getWishlistForCustomer(Long customerId) {
        // 1. 찜 목록 상품 조회 (Product + Category + Parent + Seller)
        List<Product> products = wishlistRepository.findWishlistProductsWithCategory(customerId);
        List<Long> productIds = products.stream().map(Product::getId).toList();

        // 2. 썸네일 이미지 한 번에 조회 (예: MediaType.IMAGE)
        List<ProductImage> thumbnails = productImageRepository
                .findByProductIdInAndIsThumbnailTrueAndMediaType(productIds, MediaType.IMAGE);

        // 3. productId -> 썸네일 url 맵 생성
        Map<Long, String> thumbnailMap = thumbnails.stream()
                .collect(Collectors.toMap(pi -> pi.getProduct().getId(), ProductImage::getUrl));

        // 4. DTO 변환, 썸네일이 없으면 null 처리
        return products.stream()
                .map(product -> ProductListDTO.from(product, thumbnailMap.get(product.getId())))
                .toList();
    }


}
