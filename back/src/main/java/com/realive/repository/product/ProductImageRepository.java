package com.realive.repository.product;

import com.realive.domain.product.ProductImage;
import com.realive.domain.common.enums.MediaType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    List<ProductImage> findByProductId(Long productId);

    // ✅ 썸네일 + 미디어타입(IMAGE or VIDEO)에 따라 하나만 조회
    Optional<ProductImage> findFirstByProductIdAndIsThumbnailTrueAndMediaType(Long productId, MediaType mediaType);

    // ✅ 여러 상품 ID에 대한 썸네일 이미지/영상 URL을 한 번에 조회
    @Query("SELECT pi.product.id, pi.url FROM ProductImage pi " +
            "WHERE pi.product.id IN :productIds " +
            "AND pi.isThumbnail = true " +
            "AND pi.mediaType = :mediaType")
    List<Object[]> findThumbnailUrlsByProductIds(@Param("productIds") List<Long> productIds,
                                                 @Param("mediaType") MediaType mediaType);
}
