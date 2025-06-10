// 🔹 배송 상태 enum 타입 정의
export type DeliveryStatus =
    | 'DELIVERY_PREPARING'
    | 'DELIVERY_IN_PROGRESS'
    | 'DELIVERY_COMPLETED';

// 🔹 주문 목록 응답
export interface SellerOrderResponse {
    orderId: number;
    orderStatus: string;
    totalPrice: number;
    orderCreatedAt: string;
}

// 🔹 주문 상세 응답
export interface SellerOrderDetailResponse extends SellerOrderResponse {
    deliveryStatus: DeliveryStatus;
    deliveryAddress: string;
    receiverName: string;
    phone: string;
    deliveryFee: number;
    items: {
        productId: number;
        productName: string;
        quantity: number;
        price: number;
    }[];
}

// 🔹 배송 상태 변경 요청용
export interface DeliveryStatusUpdateRequest {
    deliveryStatus: DeliveryStatus;
    trackingNumber?: string;
    carrier?: string;
}