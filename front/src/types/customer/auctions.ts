interface Auction {
  id: number;
  productId: number;
  startPrice: number;
  currentPrice: number;
  startTime: string;
  endTime: string;
  status: string;
  createdAt?: string;
  adminProduct: {
    productName: string | null;
    imageUrl?: string | null;
  };
}

interface ApiResponse<T> {
  status: number;
  message: string;
  data: T;
}

interface PaginatedAuctionResponse {
  content: Auction[];
  totalPages: number;
  number: number;
  last: boolean;
}

interface AuctionState {
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