import { DeliveryStatus } from "./sellerOrder";

export interface OrderDeliveryDetail {
    orderId: number;
    productName: string;
    buyerId: number;
    deliveryStatus: string;
    startDate?: string | null;
    completeDate?: string | null;
    trackingNumber?: string | null;
    carrier?: string | null;
}

export interface DeliveryStatusUpdateRequest {
    deliveryStatus: DeliveryStatus;
    trackingNumber?: string;
    carrier?: string;
}
