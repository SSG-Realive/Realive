package com.realive.dto.product;

import com.realive.domain.common.enums.ProductStatus;
import lombok.Data;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

/**
 * 상품 등록/수정 요청 DTO
 */
@Data
public class ProductRequestDTO {

    private String name;
    private String description;
    private int price;
    private Integer stock;
    private Integer width;
    private Integer depth;
    private Integer height;
    private ProductStatus status;
    private Long categoryId;
    private Boolean active;

    private MultipartFile imageThumbnail;           // 대표 이미지 (필수)
    private MultipartFile videoThumbnail;           // 대표 영상 (선택)

    private List<MultipartFile> subImages;          // ✅ 상세 이미지 다건 업로드 (선택)
    // 필요시 추후 확장: private List<MultipartFile> subVideos;

    private DeliveryPolicyDTO deliveryPolicy;
}