import {OrderItemResponseDTO} from "@/types/orders/orderItemResponseDTO";


export interface OrderResponseDTO {
    orderId: number;          // Spring의 Long은 TypeScript에서 number로 매핑됩니다.
    customerId: number;       // Spring의 Long은 TypeScript에서 number로 매핑됩니다.
    deliveryAddress: string;
    totalPrice: number;       // Spring의 int는 TypeScript에서 number로 매핑됩니다.
    orderStatus: string;      // enum의 description이 문자열로 들어옵니다.
    orderCreatedAt: string;   // LocalDateTime은 ISO 8601 형식의 문자열 (예: "2023-10-26T10:00:00")로 매핑됩니다.
    updatedAt: string;        // LocalDateTime은 ISO 8601 형식의 문자열로 매핑됩니다.
    orderItems: OrderItemResponseDTO[]; // 이전에 정의한 OrderItemResponseDTO 배열
    deliveryFee: number;      // Spring의 int는 TypeScript에서 number로 매핑됩니다.

    // Customer 엔티티에서 가져오는 정보
    receiverName: string;
    phone: string;

    // Order 엔티티에 직접 존재하지 않지만, DTO에서 필요한 필드들
    paymentType: string;    // 결제 방식
    deliveryStatus: string; // 배송 상태 (DeliveryStatus enum의 description 활용)
}