// 경매 관련 타입 정의
export interface AuctionResponseDTO {
  id: number;
  startPrice: number;
  currentPrice: number;
  startTime: string;
  endTime: string;
  status: string;
  createdAt: string;
  updatedAt: string;
  adminProduct: {
    id: number;
    productId: number;
    productName: string;
    productDescription: string;
    purchasePrice: number;
    purchasedFromSellerId: number | null;
    purchasedAt: string;
    auctioned: boolean;
    imageThumbnailUrl: string;
  };
}

export interface AuctionCreateRequestDTO {
  adminProductId: number;
  startPrice: number;
  startTime: string;
  endTime: string;
}

export interface AuctionUpdateRequestDTO {
  id: number;
  name: string;
  description?: string;
  startTime: string;
  endTime: string;
  startPrice: number;
  buyNowPrice?: number;
  category: string;
  sellerId: number;
  productId: number;
}

export interface AuctionCancelResponseDTO {
  auctionId: number;
  status: string;
  cancelledAt: string;
  reason?: string;
}

export interface BidResponseDTO {
  id: number;
  auctionId: number;
  auctionName: string;
  customerId: number;
  customerName: string;
  bidAmount: number;
  bidTime: string;
  isWinning: boolean;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  numberOfElements: number;
}

export interface AuctionSearchParams {
  category?: string;
  status?: string;
  page?: number;
  size?: number;
} 