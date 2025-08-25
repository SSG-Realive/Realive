// src/types/order.ts

// OrderResponseDTO의 orderItems 배열에 들어갈 객체의 타입
export interface OrderItem {
    orderItemId: number; // DTO에 맞게 타입 설정
    productId: number;
    productName: string;
    quantity: number;
    price: number;
    imageUrl?: string; // Optional
}

// OrderResponseDTO에 해당하는 메인 타입
export interface Order {
    orderId: number;
    customerId: number;
    deliveryAddress: string;
    totalPrice: number;
    orderStatus: string;
    orderCreatedAt: string; // ISO 8601 형식의 날짜 문자열
    updatedAt: string;
    orderItems: OrderItem[];
    deliveryFee: number;
    receiverName: string;
    phone: string;
    paymentType: string;
    deliveryStatus: string;
}

// Spring의 Page<T> 응답 객체에 대한 제네릭 타입
export interface Page<T> {
    content: T[];
    totalPages: number;
    totalElements: number;
    size: number;
    number: number; // 현재 페이지 번호 (0부터 시작)
    first: boolean;
    last: boolean;
    empty: boolean;
}

export interface DirectPaymentInfoDTO {
    productId: number;
    productName: string;
    quantity: number;
    price: number;
    imageUrl: string;
    // 필요하다면 백엔드에서 보내주는 다른 정보들도 추가할 수 있습니다.
}



export interface ProductQuantity {
    productId: number;
    quantity: number;
}


//   결제 처리 API의 요청 타입
//  (단일 상품 구매와 장바구니 구매 모두에 사용 가능하도록 수정)
 
export interface PayRequestDTO {
    // 공통 정보
    receiverName: string;
    phone: string;
    deliveryAddress: string;
    
    paymentMethod: 'CARD' | "CELL_PHONE" | "ACCOUNT"

    // 토스페이먼츠 정보
    paymentKey: string;
    tossOrderId: string;
    // amount 제거 - 서버에서 계산

    // ✨ 장바구니 결제 시 사용될 정보
    orderItems?: ProductQuantity[];

    // ✨ 단일 상품 결제 시 사용될 정보
    productId?: number;
    quantity?: number;
}