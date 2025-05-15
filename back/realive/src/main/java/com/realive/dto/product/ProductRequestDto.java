package com.realive.dto.product;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ProductRequestDto {
    // 상품 이름
    private String name;

    // 상품 설명
    private String description;

    // 상품 가격
    private int price;

    // 업로드할 이미지 파일 (프론트에서 전송되는 multipart 파일)
    private MultipartFile image;
}