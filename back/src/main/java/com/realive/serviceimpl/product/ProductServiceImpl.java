package com.realive.serviceimpl.product;

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

import java.util.List;
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

    @Override
    public Long createProduct(ProductRequestDto dto, Long sellerId) {
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

        if (dto.getImage() != null && !dto.getImage().isEmpty()) {
            String imageUrl = fileUploadService.upload(dto.getImage());
            ProductImage image = ProductImage.builder()
                    .url(imageUrl)
                    .isThumbnail(true)
                    .mediaType(dto.getMediaType())
                    .product(product)
                    .build();
            productImageRepository.save(image);
        }

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

    @Override
    public void updateProduct(Long productId, ProductRequestDto dto, Long sellerId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

        if (!product.getSeller().getId().equals(sellerId)) {
            throw new SecurityException("해당 상품에 대한 수정 권한이 없습니다.");
        }

        if (dto.getImage() != null && !dto.getImage().isEmpty()) {
            productImageRepository.findByProductId(productId).stream()
                    .filter(ProductImage::isThumbnail)
                    .forEach(productImageRepository::delete);

            String imageUrl = fileUploadService.upload(dto.getImage());
            ProductImage image = ProductImage.builder()
                    .url(imageUrl)
                    .isThumbnail(true)
                    .mediaType(dto.getMediaType())
                    .product(product)
                    .build();
            productImageRepository.save(image);
        }

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock() != null ? dto.getStock() : product.getStock());
        product.setWidth(dto.getWidth());
        product.setDepth(dto.getDepth());
        product.setHeight(dto.getHeight());
        product.setStatus(dto.getStatus());
        product.setActive(dto.getActive() != null ? dto.getActive() : product.isActive());

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다."));
            product.setCategory(category);
        }

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

    @Override
    public List<ProductListDto> getProductsBySeller(Long sellerId) {
        return productRepository.findBySellerId(sellerId).stream()
                .map(product -> ProductListDto.builder()
                        .id(product.getId())
                        .name(product.getName())
                        .price(product.getPrice())
                        .status(product.getStatus().name())
                        .isActive(product.isActive())
                        .thumbnailUrl(getThumbnailUrlByProductId(product.getId()))
                        .build()
                ).collect(Collectors.toList());
    }

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
                .thumbnailUrl(getThumbnailUrlByProductId(productId))
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .sellerName(product.getSeller().getName())
                .build();
    }

    @Override
    public String getThumbnailUrlByProductId(Long productId) {
        return productImageRepository.findByProductId(productId).stream()
                .filter(ProductImage::isThumbnail)
                .findFirst()
                .map(ProductImage::getUrl)
                .orElse(null);
    }
}
