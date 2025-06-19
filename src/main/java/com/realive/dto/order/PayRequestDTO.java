package com.realive.dto.order;

import com.realive.domain.common.enums.PaymentType; // PaymentType Enum 임포트
import jakarta.validation.Valid; // @Valid 어노테이션 추가 (orderItems에 적용)
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty; // List에 항목이 비어있지 않아야 할 경우
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive; // 수량이 양수여야 할 경우
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
    private PaymentType paymentMethod;

    // --- Toss Payment API 연동을 위해 추가된 필드 ---
    @NotBlank(message = "결제 키(paymentKey)는 필수입니다.")
    private String paymentKey;
    // Toss Payments Widget에서 전달하는 orderId는 String이므로, 기존 Long type orderId와 혼동 방지를 위해 새로운 필드를 사용하는 것이 좋습니다.
    // 여기서는 PayRequestDTO의 orderId(주문 식별자)가 아닌 Toss Payments 측의 orderId를 의미합니다.
    // 만약 PayRequestDTO의 orderId가 우리 시스템의 Order ID를 의미한다면, 이 필드명은 tossOrderId와 같이 명확히 구분해야 합니다.
    // 현재는 PayRequestDTO에 productId만 있고 orderId는 없으므로, 이 orderId는 토스 위젯에서 생성한 임시 orderId로 간주하고 DTO에 추가합니다.
    @NotBlank(message = "토스페이먼츠 주문 ID(tossOrderId)는 필수입니다.")
    private String tossOrderId; // 토스페이먼츠 위젯에서 생성된 orderId
}