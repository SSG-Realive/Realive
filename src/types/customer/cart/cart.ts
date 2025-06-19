export interface CartItem {
    cartItemId: number;
    productId: number;
    productName: string;
    imageThumbnailUrl: string;
    productPrice: number;
    quantity: number;
    stock: number;
}

export interface CartItemAddRequest {
    productId: number;
    quantity: number;
}

export interface CartListResponse {
    items: CartItem[];
    totalQuantity: number;
}
