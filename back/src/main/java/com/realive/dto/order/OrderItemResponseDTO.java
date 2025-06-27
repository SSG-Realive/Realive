package com.realive.dto.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponseDTO {

    private long id;
    private long productId;
    private String productName;
    private int quantity;
    private int price;
    private String imageUrl;

    // 프론트에서 review를 작성 할 때, sellerId를 사용하기 위해 추가
    @JsonProperty("sellerId")
    private Long sellerId;
}