package com.realive.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerateProductDescriptionRequestDTO {
    private String productName; // 예: "고무나무 원목 식탁"
    private String categoryName; // 예: "주방 가구"
    private List<String> features; // 예: ["6인용", "친환경 원목", "접이식"]
    private String tone; // 예: "정중한", "유쾌한", "간결한", etc.
}
