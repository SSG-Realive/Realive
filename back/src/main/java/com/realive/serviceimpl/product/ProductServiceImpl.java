package com.realive.serviceimpl.product;

import com.realive.domain.common.enums.MediaType;
import com.realive.domain.product.*;
import com.realive.domain.seller.Seller;
import com.realive.dto.product.*;
import com.realive.repository.product.*;
import com.realive.repository.seller.SellerRepository;
import com.realive.service.common.FileUploadService;
import com.realive.service.product.ProductService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SellerRepository sellerRepository;
    private final DeliveryPolicyRepository deliveryPolicyRepository;
    private final FileUploadService fileUploadService;

    /**
     * 상품 등록
     */
    @Override
    public Long createProduct(ProductRequestDto dto, Long sellerId) {
        // ✅ 대표 이미지 유효성 검사
        if (dto.getImageThumbnail() == null || dto.getImageThumbnail().isEmpty()) {
            throw new IllegalArgumentException("대표 이미지는 필수입니다.");
        }

        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 판매자입니다."));

        Category category = null;
        if (dto.getCategoryId() != null) {
            category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다."));
        }

        Product product = Product.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .stock(dto.getStock() != null ? dto.getStock() : 1)
                .width(dto.getWidth())
                .depth(dto.getDepth())
                .height(dto.getHeight())
                .status(dto.getStatus())
                .isActive(dto.getActive() != null ? dto.getActive() : true)
                .category(category)
                .seller(seller)
                .build();

        productRepository.save(product);

        // 대표 이미지 저장
        String imageUrl = fileUploadService.upload(dto.getImageThumbnail(), "product", sellerId);
        productImageRepository.save(ProductImage.builder()
                .url(imageUrl)
                .isThumbnail(true)
                .mediaType(MediaType.IMAGE)
                .product(product)
                .build());

        // 대표 영상 저장 (선택)
        if (dto.getVideoThumbnail() != null && !dto.getVideoThumbnail().isEmpty()) {
            String videoUrl = fileUploadService.upload(dto.getVideoThumbnail(), "product", sellerId);
            productImageRepository.save(ProductImage.builder()
                    .url(videoUrl)
                    .isThumbnail(true)
                    .mediaType(MediaType.VIDEO)
                    .product(product)
                    .build());
        }

        // ✅ 서브 이미지 저장
        if (dto.getSubImages() != null && !dto.getSubImages().isEmpty()) {
            for (MultipartFile file : dto.getSubImages()) {
                if (file != null && !file.isEmpty()) {
                    String url = fileUploadService.upload(file, "product", sellerId);
                    productImageRepository.save(ProductImage.builder()
                            .url(url)
                            .isThumbnail(false)
                            .mediaType(MediaType.IMAGE)
                            .product(product)
                            .build());
                }
            }
        }

        // 배송 정책 저장
        if (dto.getDeliveryPolicy() != null) {
            DeliveryPolicy policy = DeliveryPolicy.builder()
                    .type(dto.getDeliveryPolicy().getType())
                    .cost(dto.getDeliveryPolicy().getCost())
                    .regionLimit(dto.getDeliveryPolicy().getRegionLimit())
                    .product(product)
                    .build();
            deliveryPolicyRepository.save(policy);
        }

        return product.getId();
    }

    /**
     * 상품 수정
     */
    @Override
    public void updateProduct(Long productId, ProductRequestDto dto, Long sellerId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

        if (!product.getSeller().getId().equals(sellerId)) {
            throw new SecurityException("해당 상품에 대한 수정 권한이 없습니다.");
        }

        // 기존 썸네일 이미지/영상 삭제
        productImageRepository.findByProductId(productId).stream()
                .filter(ProductImage::isThumbnail)
                .forEach(productImageRepository::delete);

        // 대표 이미지 저장
        if (dto.getImageThumbnail() != null && !dto.getImageThumbnail().isEmpty()) {
            String imageUrl = fileUploadService.upload(dto.getImageThumbnail(), "product", sellerId);
            productImageRepository.save(ProductImage.builder()
                    .url(imageUrl)
                    .isThumbnail(true)
                    .mediaType(MediaType.IMAGE)
                    .product(product)
                    .build());
        }

        // 대표 영상 저장 (선택)
        if (dto.getVideoThumbnail() != null && !dto.getVideoThumbnail().isEmpty()) {
            String videoUrl = fileUploadService.upload(dto.getVideoThumbnail(), "product", sellerId);
            productImageRepository.save(ProductImage.builder()
                    .url(videoUrl)
                    .isThumbnail(true)
                    .mediaType(MediaType.VIDEO)
                    .product(product)
                    .build());
        }

        // 서브 이미지 저장 (선택)
        if (dto.getSubImages() != null && !dto.getSubImages().isEmpty()) {
            for (MultipartFile file : dto.getSubImages()) {
                if (file != null && !file.isEmpty()) {
                    String url = fileUploadService.upload(file, "product", sellerId);
                    productImageRepository.save(ProductImage.builder()
                            .url(url)
                            .isThumbnail(false)
                            .mediaType(MediaType.IMAGE)
                            .product(product)
                            .build());
                }
            }
        }

        // 상품 정보 수정
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock() != null ? dto.getStock() : product.getStock());
        product.setWidth(dto.getWidth());
        product.setDepth(dto.getDepth());
        product.setHeight(dto.getHeight());
        product.setStatus(dto.getStatus());
        product.setActive(dto.getActive() != null ? dto.getActive() : product.isActive());

        // 카테고리 수정
        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다."));
            product.setCategory(category);
        }

        // 배송 정책 수정
        if (dto.getDeliveryPolicy() != null) {
            DeliveryPolicy policy = deliveryPolicyRepository.findByProduct(product)
                    .orElse(new DeliveryPolicy());

            policy.setProduct(product);
            policy.setType(dto.getDeliveryPolicy().getType());
            policy.setCost(dto.getDeliveryPolicy().getCost());
            policy.setRegionLimit(dto.getDeliveryPolicy().getRegionLimit());

            deliveryPolicyRepository.save(policy);
        }

        productRepository.save(product);
    }

    /**
     * 상품 삭제
     */
    @Override
    public void deleteProduct(Long productId, Long sellerId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

        if (!product.getSeller().getId().equals(sellerId)) {
            throw new SecurityException("해당 상품에 대한 삭제 권한이 없습니다.");
        }

        productImageRepository.findByProductId(productId)
                .forEach(productImageRepository::delete);

        deliveryPolicyRepository.findByProduct(product)
                .ifPresent(deliveryPolicyRepository::delete);

        productRepository.delete(product);
    }

    /**
     * 판매자별 상품 목록 조회 (이미지 + 영상 썸네일 모두 포함)
     */
    @Override
    public List<ProductListDto> getProductsBySeller(Long sellerId) {
        return productRepository.findBySellerId(sellerId).stream()
                .map(product -> ProductListDto.builder()
                        .id(product.getId())
                        .name(product.getName())
                        .price(product.getPrice())
                        .status(product.getStatus().name())
                        .isActive(product.isActive())
                        .imageThumbnailUrl(getThumbnailUrlByType(product.getId(), MediaType.IMAGE))
                        .videoThumbnailUrl(getThumbnailUrlByType(product.getId(), MediaType.VIDEO))
                        .build()
                ).collect(Collectors.toList());
    }

    /**
     * 상품 상세 조회
     */
    @Override
    public ProductResponseDto getProductDetail(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        return ProductResponseDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .width(product.getWidth())
                .depth(product.getDepth())
                .height(product.getHeight())
                .status(product.getStatus().name())
                .isActive(product.isActive())
                .imageThumbnailUrl(getThumbnailUrlByType(productId, MediaType.IMAGE))
                .videoThumbnailUrl(getThumbnailUrlByType(productId, MediaType.VIDEO))
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .sellerName(product.getSeller().getName())
                .build();
    }

    /**
     * 썸네일 이미지 or 영상 URL 조회
     */
    public String getThumbnailUrlByType(Long productId, MediaType mediaType) {
        return productImageRepository
                .findFirstByProductIdAndIsThumbnailTrueAndMediaType(productId, mediaType)
                .map(ProductImage::getUrl)
                .orElse(null);
    }
}
