export interface ReviewResponseDTO {
    reviewId: number;
    orderId: number;
    customerId: number;
    sellerId: number;
    productId: number;
    productName: string;
    rating: number;
    content: string;
    imageUrls: string[];
    createdAt: string;
    isHidden: boolean;

    productSummaryList?: ProductSummaryDTO[];
}

export interface ReviewCreateRequestDTO {
  orderId: number;
  sellerId: number;
  rating: number;
  content: string;
  imageUrls: string[];
}

export interface ProductSummaryDTO {
  id: number;
  name: string;
  imageThumbnailUrl: string;
}

export interface ReviewCreateRequestDTO {
  orderId: number;
  sellerId: number;
  rating: number;
  content: string;
  imageUrls: string[];
}
