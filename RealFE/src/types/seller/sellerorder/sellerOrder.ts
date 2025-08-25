// 🔹 배송 상태 enum 타입 정의
export type DeliveryStatus =
    | "INIT"
    | 'DELIVERY_PREPARING'
    | 'DELIVERY_IN_PROGRESS'
    | 'DELIVERY_COMPLETED'
    | 'CANCELLED';

// 🔹 주문 목록 응답
export interface SellerOrderResponse {
    orderId: number;
    orderedAt: string;
    customerName: string;
    productName: string;
    quantity: number;
    deliveryStatus: string;
    deleiveryStatusText: string | null;
    trackingNumber: string | null;
    startDate: string;
    completeDate: string;
    deliveryType: string | null;
}

// 🔹 주문 상세 응답 (새로운 API에 맞게 업데이트)
export interface SellerOrderDetailResponse {
    orderId: number;
    orderedAt: string;
    customerName: string;
    customerPhone: string;
    deliveryAddress: string;
    receiverName: string;
    totalPrice: number;
    deliveryFee: number;
    paymentType: string;
    deliveryStatus: DeliveryStatus;
    trackingNumber: string | null;
    carrier: string | null;
    startDate: string | null;
    completeDate: string | null;
    deliveryType: string | null;
    items: {
        productId: number;
        productName: string;
        quantity: number;
        price: number;
        imageUrl: string | null;
    }[];
}

// 🔹 배송 상태 변경 요청용
export interface DeliveryStatusUpdateRequest {
    deliveryStatus: DeliveryStatus;
    trackingNumber?: string;
    carrier?: string;
}