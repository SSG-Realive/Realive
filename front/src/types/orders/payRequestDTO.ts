

// Spring의 PaymentType enum에 상응하는 TypeScript 타입
// 💡 실제 Spring PaymentType enum의 값들을 문자열 리터럴 유니온 타입으로 정확히 수정해주세요!
// 예: "CARD" | "BANK_TRANSFER" | "NAVER_PAY" | "TOSS_PAY" | "MOBILE_PAY"
import {ProductQuantityDTO} from "@/types/orders/productQuantityDTO";

export type PaymentType = "CARD" | "BANK_TRANSFER" | "NAVER_PAY" | "TOSS_PAY" | "MOBILE_PAY";

export interface PayRequestDTO {
    customerId: number; // @NotNull -> 필수 필드

    // --- 결제 상품 정보 (두 그룹 중 하나만 유효하도록 하는 로직은 프론트엔드에서도 처리해야 합니다.) ---
    // [옵션 1] 단일 상품 결제 시 사용
    productId?: number;
    quantity?: number;

    // [옵션 2] 여러 상품 (장바구니) 결제 시 사용
    orderItems?: ProductQuantityDTO[];

    receiverName: string;    // @NotBlank -> 필수 필드
    phone: string;           // @NotBlank -> 필수 필드
    deliveryAddress: string; // @NotBlank -> 필수 필드

    paymentMethod: PaymentType; // @NotNull -> 필수 필드, 정의된 PaymentType 유니온 타입 사용
}