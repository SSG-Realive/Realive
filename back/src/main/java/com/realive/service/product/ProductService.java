package com.realive.service.product;

import com.realive.domain.product.Product;
import com.realive.domain.seller.Seller;
import com.realive.dto.product.ProductRequestDto;
import com.realive.dto.product.ProductResponse;
import com.realive.repository.product.ProductRepository;
import com.realive.repository.seller.SellerRepository;
import com.realive.service.common.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final SellerRepository sellerRepository;
    private final FileUploadService fileUploadService;

    // 상품 등록 로직
    public Long createProduct(ProductRequestDto dto, Long sellerId) {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new RuntimeException("판매자 없음"));

        // 이미지 업로드 처리
        String imageUrl = fileUploadService.upload(dto.getImage());

        // Product 엔티티 생성
        Product product = Product.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .imageUrl(imageUrl)
                .seller(seller)
                .build();

        // 저장 후 상품 ID 반환
        return productRepository.save(product).getId();
    }

    // 로그인한 판매자가 등록한 상품 목록 조회
    public List<ProductResponse> getProductsBySeller(Long sellerId) {
        return productRepository.findBySellerId(sellerId).stream()
                .map(p -> ProductResponse.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .description(p.getDescription())
                        .price(p.getPrice())
                        .imageUrl(p.getImageUrl())
                        .sellerName(p.getSeller().getName())
                        .build())
                .collect(Collectors.toList());
    }
}