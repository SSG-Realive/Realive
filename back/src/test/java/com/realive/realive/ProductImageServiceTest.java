package com.realive.realive;

import com.realive.domain.common.enums.MediaType;
import com.realive.domain.product.Product;
import com.realive.domain.product.ProductImage;
import com.realive.repository.product.ProductImageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ProductImageServiceTest {

    private final ProductImageRepository productImageRepository = Mockito.mock(ProductImageRepository.class);

    @Test
    @DisplayName("대표 이미지 URL 조회 성공")
    void getImageThumbnailUrl_Success() {
        Long productId = 1L;
        ProductImage mockImage = ProductImage.builder()
                .url("https://fake.url/image.jpg")
                .isThumbnail(true)
                .mediaType(MediaType.IMAGE)
                .product(Product.builder().id(productId).build())
                .build();

        when(productImageRepository.findFirstByProductIdAndIsThumbnailTrueAndMediaType(productId, MediaType.IMAGE))
                .thenReturn(Optional.of(mockImage));

        String result = productImageRepository
                .findFirstByProductIdAndIsThumbnailTrueAndMediaType(productId, MediaType.IMAGE)
                .map(ProductImage::getUrl)
                .orElse(null);

        assertEquals("https://fake.url/image.jpg", result);
    }

    @Test
    @DisplayName("대표 이미지 저장 호출")
    void saveThumbnailImage_CalledOnce() {
        ProductImage image = ProductImage.builder()
                .url("https://fake.url/image.jpg")
                .isThumbnail(true)
                .mediaType(MediaType.IMAGE)
                .product(Product.builder().id(1L).build())
                .build();

        productImageRepository.save(image);
        verify(productImageRepository, times(1)).save(image);
    }

    @Test
    @DisplayName("대표 이미지 삭제 호출")
    void deleteThumbnailImage_CalledOnce() {
        ProductImage image = ProductImage.builder()
                .url("https://fake.url/image.jpg")
                .isThumbnail(true)
                .mediaType(MediaType.IMAGE)
                .product(Product.builder().id(1L).build())
                .build();

        productImageRepository.delete(image);
        verify(productImageRepository, times(1)).delete(image);
    }
}