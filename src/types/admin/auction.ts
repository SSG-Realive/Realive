// 경매 관련 타입 정의
export interface AuctionResponseDTO {
  id: number;
  name: string;
  description?: string;
  startTime: string;
  endTime: string;
  startPrice: number;
  currentPrice?: number;
  buyNowPrice?: number;
  status: 'ACTIVE' | 'ENDED' | 'CANCELLED';
  category: string;
  sellerId: number;
  sellerName: string;
  productId: number;
  productName: string;
  productImage?: string;
  winnerId?: number;
  winnerName?: string;
  winningPrice?: number;
  createdAt: string;
  updatedAt: string;
}

export interface AuctionCreateRequestDTO {
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