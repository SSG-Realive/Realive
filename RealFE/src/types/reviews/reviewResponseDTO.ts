
export interface ReviewResponseDTO {
    reviewId: number;
    orderId: number;
    customerId: number;
    sellerId: number;
    productId: number;
    // productName: string;
    rating: number;
    content: string;
    imageUrls: string[];
    createdAt: string;
    isHidden: boolean;
}