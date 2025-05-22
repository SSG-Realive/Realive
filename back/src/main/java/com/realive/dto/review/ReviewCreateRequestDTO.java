package com.realive.dto.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ReviewCreateRequestDTO {

    @NotNull(message = "주문 ID는 필수입니다.")
    private Long orderId;

    @NotNull(message = "판매자 ID는 필수입니다.") // 생성 시 판매자 ID도 필요할 수 있습니다.
    private Long sellerId;

    // 고객 ID는 컨트롤러에서 보안 컨텍스트에서 가져오는 것이 이상적입니다.
    // 만약 당장 보안 컨텍스트에서 가져올 수 없다면, 임시로 요청에 포함시킬 수 있지만,
    // 장기적으로는 서버에서 인증된 사용자 정보를 통해 가져오는 것이 맞습니다.
     private Long customerId;

    @NotNull(message = "평점은 필수입니다.")
    @Min(value = 1, message = "평점은 최소 1점입니다.")
    @Max(value = 5, message = "평점은 최대 5점입니다.")
    private Double rating;

    @NotBlank(message = "내용은 필수입니다.")
    private String content;

    private List<String> imageUrls;

    public interface CreateValidation {}
}