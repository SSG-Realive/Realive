//types/customer/auctions.ts
import type { AdminProduct } from './adminProduct';


export interface Auction {
  id: number;
  productId: number;
  startPrice: number;
  currentPrice: number;
  startTime: string;
  endTime: string;
  status: string;
  createdAt?: string;
  adminProduct: AdminProduct | null;
}


export interface ApiResponse<T> {
  status: number;
  message: string;
  data: T;
}

export interface PaginatedAuctionResponse {
  content: Auction[];
  totalPages: number;
  number: number;
  last: boolean;
}

export interface AuctionState {
  auctions: Auction[];
  category: string;
  page: number;
  hasNext: boolean;
  loading: boolean;
  error: string | null;
  lastFetchTime: number; // 중복 요청 방지용
  setCategory: (category: string) => void;
  reset: () => void;
  fetchAuctions: () => Promise<void>;
}

export interface Bid {
  id: number;
  auctionId: number;
  customerId: number;
  customerName: string; // DTO에 따라 추가될 수 있습니다.
  bidPrice: number;
  bidTime: string;
  isWinning: boolean;
  leading?: boolean;
}

export interface PaginatedBidResponse {
  content: Bid[];
  totalPages: number;
  number: number;
  last: boolean;
}

export interface PlaceBidRequest {
  auctionId: number;
  bidPrice: number;
}

export interface GetAuctionsParams {
  category?: string; // 카테고리는 선택사항
  page: number;
  size: number;
  sort?: string; // 정렬 파라미터 (예: 'endTime,asc')
  status?: string; // 상태별 조회 (예: 'PROCEEDING')
}

export interface WonAuction {
  auctionId: number;       // id -> auctionId
  productName: string;
  productImageUrl: string; // thumbnailUrl -> productImageUrl
  winningBidPrice: number; // finalBidPrice -> winningBidPrice
  auctionEndTime: string;  // auctionEndDate -> auctionEndTime
}

export interface WonAuctionState {
  auctions: WonAuction[];
  page: number;
  hasNext: boolean;
  loading: boolean;
  error: string | null;
  fetchWonAuctions: () => Promise<void>;
  reset: () => void;
}

export interface AuctionWinInfo {
  auctionId: number;
  productName: string;
  productImageUrl: string | null;
  winningBidPrice: number;
  isPaid: boolean;
}
export interface UserProfile {
  receiverName: string;
  phone: string;
  deliveryAddress: string;
  email?: string;
}
export interface AuctionPaymentRequestDTO {
  auctionId: number;
  paymentKey: string;
  amount: number;
}