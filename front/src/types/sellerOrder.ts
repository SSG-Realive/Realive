export interface SellerOrderResponse {
    orderId: number;
    orderStatus: string;
    totalPrice: number;
    orderCreatedAt: string;
}

export interface SellerOrderDetailResponse extends SellerOrderResponse {
    deliveryStatus: string;
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