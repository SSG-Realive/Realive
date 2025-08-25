package com.realive.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayRequestDTO {

    @NotNull(message = "고객 ID는 필수입니다.")
    private Long customerId;

    // --- 결제 상품 정보 (두 그룹 중 하나만 유효하도록 하는 로직이 필요) ---
    // [옵션 1] 단일 상품 결제 시 사용
    private Long productId;
    @Positive(message = "수량은 1개 이상이어야 합니다.")
    private Integer quantity; // Integer로 null 값 허용

    // [옵션 2] 여러 상품 (장바구니) 결제 시 사용
    // @Valid: ProductQuantityDTO 내의 유효성 검사도 수행하도록 함
    // @NotEmpty: 리스트가 비어있지 않아야 할 경우 사용. (선택적)
    private List<@Valid ProductQuantityDTO> orderItems;

    // NOTE: productId/quantity 와 orderItems 중 하나만 유효하도록 하는 유효성 검사는
    // Custom Validator를 구현하거나 서비스/컨트롤러 로직에서 명시적으로 처리하는 것이 좋습니다.
    // 예: if (productId != null && orderItems != null) throw new IllegalArgumentException("단일 상품과 여러 상품 결제를 동시에 요청할 수 없습니다.");

    @NotBlank(message = "수령인 이름은 필수입니다.")
    private String receiverName;

    @NotBlank(message = "전화번호는 필수입니다.")
    private String phone;

    @NotBlank(message = "배송 주소는 필수입니다.")
    private String deliveryAddress;

    @NotNull(message = "결제 방법은 필수입니다.")
    private String paymentMethod;

    // --- Toss Payment API 연동 ---
    @NotBlank(message = "결제 키(paymentKey)는 필수입니다.")
    private String paymentKey;

    @NotBlank(message = "토스페이먼츠 주문 ID(tossOrderId)는 필수입니다.")
    private String tossOrderId; // 토스페이먼츠 위젯에서 생성된 orderId
}