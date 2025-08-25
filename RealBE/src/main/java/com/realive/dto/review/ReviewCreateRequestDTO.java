package com.realive.dto.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

//[Customer] 리뷰 생성 요청 DTO

@Data
public class ReviewCreateRequestDTO {

    @NotNull(message = "주문 ID는 필수입니다.")
    private Long orderId;

    @NotNull(message = "판매자 ID는 필수입니다.") // 생성 시 판매자 ID도 필요할 수 있습니다.
    private Long sellerId;

    private Long customerId;

    @NotNull(message = "평점은 필수입니다.")
    @Min(value = 1, message = "평점은 최소 1점입니다.")
    @Max(value = 5, message = "평점은 최대 5점입니다.")
    private Double rating;

    @NotBlank(message = "내용은 필수입니다.")
    private String content;

    private List<String> imageUrls;

    /*유효성 검증 그룹을 지정하기 위한 마커 인터페이스입니다.*/
    public interface CreateValidation {}

}