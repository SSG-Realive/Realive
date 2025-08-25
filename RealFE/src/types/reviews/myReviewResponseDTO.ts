
export interface MyReviewResponseDTO {
    reviewId: number;
    orderId: number;
    productName: string;
    rating: number;
    content: string;
    imageUrls: string[];
    createdAt: string;
}