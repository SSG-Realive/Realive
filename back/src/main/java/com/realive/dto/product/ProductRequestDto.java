package com.realive.dto.product;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ProductRequestDto {

    // 상품명 (등록/수정 공통)
    private String name;

    // 설명 (공통)
    private String description;

    // 가격 (공통)
    private int price;

    // 재고 (기본 1)
    private int stock = 1;

    // 크기 정보 (선택)
    private Integer width;
    private Integer depth;
    private Integer height;

    // 상태 (상/중/하) - 기본 "상"
    private String status = "상";

    // 카테고리 ID (nullable)
    private Long categoryId;

    // 활성 상태 (기본 true)
    private Boolean isActive = true;

    // 이미지 업로드 (등록 시 필수, 수정 시 선택)
    private MultipartFile image;
}
