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

    // 현재 시스템 제약사항에 따라 customerId를 DTO에 포함
    @NotNull(message = "고객 ID는 필수입니다.")
    private Long customerId;

    // --- 결제 상품 정보 (두 그룹 중 하나만 유효하도록 하는 로직이 필요) ---
    // [옵션 1] 단일 상품 결제 시 사용
    private Long productId;
    @Positive(message = "수량은 1개 이상이어야 합니다.") // 수량은 1개 이상
    private Integer quantity; // Integer로 변경하여 productId와 함께 null 체크 용이하게

    // [옵션 2] 여러 상품 (장바구니) 결제 시 사용
    // @Valid: ProductQuantityDTO 내의 유효성 검사도 수행하도록 함
    // @NotEmpty: 리스트가 비어있지 않아야 할 경우 사용. (선택적)
    private List<@Valid ProductQuantityDTO> orderItems;

    // NOTE: productId/quantity 와 orderItems 중 하나만 유효하도록 하는 유효성 검사는
    // Custom Validator를 구현하거나 서비스/컨트롤러 로직에서 명시적으로 처리하는 것이 좋습니다.
    // 예: if (productId != null && orderItems != null) throw new IllegalArgumentException("단일 상품과 여러 상품 결제를 동시에 요청할 수 없습니다.");


    // --- 배송 정보 ---
    @NotBlank(message = "수령인 이름은 필수입니다.")
    private String receiverName;

    @NotBlank(message = "전화번호는 필수입니다.")
    private String phone;

    @NotBlank(message = "배송 주소는 필수입니다.")
    private String deliveryAddress;

    // --- 결제 방법 ---
    @NotNull(message = "결제 방법은 필수입니다.")
    private PaymentType paymentMethod; // String 대신 Enum 타입 사용
}