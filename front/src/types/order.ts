export interface OrderItemResponse {
    productId: number;
    productName: string;
    price: number;
    quantity: number;
}

export interface OrderResponse {
    orderId: number;
    customerId: number;
    deliveryAddress: string;
    totalPrice: number;
    orderStatus: string;
    orderCreatedAt: string;
    updatedAt: string;
    deliveryFee: number;
    receiverName: string;
    phone: string;
    paymentType: string;
    orderItems: OrderItemResponse[];
}

export interface PageResponse<T> {
    content: T[];
    totalPages: number;
    totalElements: number;
    page: number;
    size: number;
    hasNext: boolean;
}
