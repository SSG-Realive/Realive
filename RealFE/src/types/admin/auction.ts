// 경매 관련 타입 정의
export interface AdminProduct {
  id: number;
  productId: number;
  productName: string;
  productDescription: string;
  purchasePrice: number;
  purchasedFromSellerId: number | null;
  purchasedAt: string;
  auctioned: boolean;
  imageThumbnailUrl: string;
  imageUrls: string[];
}

export interface AuctionResponseDTO {
  id: number;
  name: any;
  startPrice: number;
  currentPrice: number;
  startTime: string;
  endTime: string;
  status: string;
  statusText?: string;
  createdAt: string;
  updatedAt: string;
  adminProduct: AdminProduct;
}

export interface AuctionCreateRequestDTO {
  adminProductId: number;
  startPrice: number;
  startTime: string;
  endTime: string;
}

export interface AuctionUpdateRequestDTO {
  id: number;
  startTime?: string;
  endTime?: string;
}

export interface AuctionCancelResponseDTO {
  auctionId: number;
  status: string;
  cancelledAt: string;
  reason?: string;
}

// 입찰 관련 타입 정의
export interface BidResponseDTO {
  id: number;
  auctionId: number;
  customerId: number;
  customerName: string;
  bidPrice: number;
  bidTime: string;
}

export interface BidRequestDTO {
  auctionId: number;
  bidPrice: number;
}

export interface BidHistoryDTO {
  bidId: number;
  customerId: number;
  customerName: string;
  bidPrice: number;
  bidTime: string;
  bidStatus: string;
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