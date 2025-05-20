package com.realive.repository.product;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.realive.domain.product.ProductImage;
import com.realive.domain.common.enums.MediaType;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    List<ProductImage> findByProductId(Long productId);

    // ✅ 썸네일 + 미디어타입(IMAGE or VIDEO)에 따라 하나만 조회
    Optional<ProductImage> findFirstByProductIdAndIsThumbnailTrueAndMediaType(Long productId, MediaType mediaType);

}