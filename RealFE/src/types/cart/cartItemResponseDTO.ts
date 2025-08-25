
export interface CartItemResponseDTO {
    cartItemId: number;
    productId: number | null;
    productName: string | null;
    quantity: number;
    productPrice: number;
    productImage: string | null;
    totalPrice: number;
    cartCreatedAt: string;
}