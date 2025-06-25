
export interface ReviewUpdateRequestDTO {
    customerId: number;
    rating: number;
    content?: string;
    imageUrls?: string[];
}