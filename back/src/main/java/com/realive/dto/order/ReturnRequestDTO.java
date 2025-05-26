package com.realive.dto.order;

import com.realive.domain.common.enums.ReturnReason;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class ReturnRequestDTO {

    @NotNull(message = "주문 번호는 필수입니다.")
    private Long orderId;

    @NotNull(message = "반품할 상품 번호는 필수입니다.")
    private Long productId;

    @Min(value = 1, message = "반품 수량은 1 이상이여야 합니다.")
    private int quantity;

    @NotNull(message = "반품 사유는 필수입니다.")
    private ReturnReason returnReason;

    private String detailedReason; // 기타 사유 선택 시 상세 내용

    // 하자가 있는 제품임을 인증하기 위한 사진 파일 (컨트롤러에서 서비스로 전달)
    private List<MultipartFile> evidenceImages;
}