// 판매자 리뷰 관련 타입 정의

export interface SellerReviewResponse {
  reviewId: number;
  orderId: number;
  customerId: number;
  sellerId: number;
  productName: string;
  rating: number;
  content: string;
  imageUrls: string[];
  createdAt: string; // LocalDateTime이 JSON에서 string으로 변환됨
  isHidden: boolean;
}

export interface SellerReviewListResponse {
  reviews: SellerReviewResponse[];
  totalCount: number;
  page: number;
  size: number;
}

export interface SellerReviewStatistics {
  totalReviews: number;
  averageRating: number;
  rating5Count: number;
  rating4Count: number;
  rating3Count: number;
  rating2Count: number;
  rating1Count: number;
}

export interface ReviewFilterOptions {
  productName?: string;
  rating?: number;
  sortBy?: 'latest' | 'oldest' | 'rating_high' | 'rating_low';
} 