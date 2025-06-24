
export interface ReviewCreateRequestDTO {
    orderId: number;
    sellerId: number;
    customerId?: number;
    rating: number;
    content: string;
    imageUrls?: string[];
}