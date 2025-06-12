package com.realive.dto.product;

import com.realive.domain.common.enums.ProductStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 상품 등록/수정 요청 DTO
 */
@Data
public class ProductRequestDTO {

    @NotBlank
    private String name;

    @NotNull
    @Min(1)
    private Integer price;

    @NotBlank
    private String description;

    @Min(0)
    private Integer stock;

    @Min(0)
    private Integer width;

    @Min(0)
    private Integer depth;

    @Min(0)
    private Integer height;

    private ProductStatus status;

    private Long categoryId;

    private Boolean active;

    @NotNull
    private MultipartFile imageThumbnail;

    private MultipartFile videoThumbnail;

    private List<MultipartFile> subImages;

    @Valid
    private DeliveryPolicyDTO deliveryPolicy;
}