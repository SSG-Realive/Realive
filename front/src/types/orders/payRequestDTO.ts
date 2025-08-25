import {ProductQuantityDTO} from "@/types/orders/productQuantityDTO";

//현재 CARD 결제만 허용
export type PaymentType = "CARD";

export interface PayRequestDTO {
    // customerId는 백엔드에서 @AuthenticationPrincipal로 처리하므로 optional로 변경
    customerId?: number;

    // --- 결제 상품 정보 (두 그룹 중 하나만 유효하도록 하는 로직은 프론트엔드에서도 처리해야 합니다.) ---
    // [옵션 1] 단일 상품 결제 시 사용
    productId?: number;
    quantity?: number;

    // [옵션 2] 여러 상품 (장바구니) 결제 시 사용
    orderItems?: ProductQuantityDTO[];

    receiverName: string;
    phone: string;
    deliveryAddress: string;

    paymentMethod: PaymentType;
}