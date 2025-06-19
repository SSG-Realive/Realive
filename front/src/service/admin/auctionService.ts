import { adminApi } from '@/app/lib/axios';
import { 
  AuctionResponseDTO, 
  AuctionCreateRequestDTO, 
  AuctionUpdateRequestDTO, 
  AuctionCancelResponseDTO,
  BidResponseDTO,
  PageResponse,
  AuctionSearchParams
} from '@/types/admin/auction';

const BASE_URL = '/admin';

export const adminAuctionService = {
  // 경매 목록 조회
  async getAuctions(params?: AuctionSearchParams): Promise<PageResponse<AuctionResponseDTO>> {
    const searchParams = new URLSearchParams();
    
    if (params?.category) searchParams.append('category', params.category);
    if (params?.status) searchParams.append('status', params.status);
    if (params?.page) searchParams.append('page', params.page.toString());
    if (params?.size) searchParams.append('size', params.size.toString());

    const response = await adminApi.get(`${BASE_URL}/auctions?${searchParams.toString()}`);
    // Postman 응답 구조에 맞게 수정
    const content = response.data.data?.content || [];
    return {
      content: content,
      totalElements: response.data.data?.totalElements || 0,
      totalPages: response.data.data?.totalPages || 0,
      size: response.data.data?.size || 10,
      number: response.data.data?.number || 0,
      numberOfElements: response.data.data?.numberOfElements || 0,
      first: response.data.data?.first || false,
      last: response.data.data?.last || false
    };
  },

  // 경매 상세 조회
  async getAuctionById(auctionId: number): Promise<AuctionResponseDTO> {
    const response = await adminApi.get(`${BASE_URL}/auctions/${auctionId}`);
    return response.data.data;
  },

  // 경매 등록
  async createAuction(auctionData: AuctionCreateRequestDTO): Promise<AuctionResponseDTO> {
    const response = await adminApi.post(`${BASE_URL}/auctions`, auctionData);
    return response.data.data;
  },

  // 경매 수정
  async updateAuction(auctionId: number, auctionData: AuctionUpdateRequestDTO): Promise<AuctionResponseDTO> {
    const response = await adminApi.put(`${BASE_URL}/auctions/${auctionId}`, auctionData);
    return response.data.data;
  },

  // 경매 취소
  async cancelAuction(auctionId: number, reason?: string): Promise<AuctionCancelResponseDTO> {
    const params = reason ? `?reason=${encodeURIComponent(reason)}` : '';
    const response = await adminApi.post(`${BASE_URL}/auctions/${auctionId}/cancel${params}`);
    return response.data.data;
  },

  // 판매자별 경매 목록 조회
  async getAuctionsBySeller(sellerId: number, page = 0, size = 10): Promise<PageResponse<AuctionResponseDTO>> {
    const response = await adminApi.get(`${BASE_URL}/auctions/seller/${sellerId}?page=${page}&size=${size}`);
    const content = response.data.data?.content || [];
    return {
      content: content,
      totalElements: response.data.data?.totalElements || 0,
      totalPages: response.data.data?.totalPages || 0,
      size: response.data.data?.size || 10,
      number: response.data.data?.number || 0,
      numberOfElements: response.data.data?.numberOfElements || 0,
      first: response.data.data?.first || false,
      last: response.data.data?.last || false
    };
  }
};

export const adminBidService = {
  // 특정 경매의 입찰 내역 조회
  async getBidsByAuction(auctionId: number, page = 0, size = 20): Promise<PageResponse<BidResponseDTO>> {
    const response = await adminApi.get(`${BASE_URL}/bids/auction/${auctionId}?page=${page}&size=${size}`);
    return response.data.data;
  },

  // 고객별 입찰 내역 조회
  async getBidsByCustomer(customerId: number, page = 0, size = 20): Promise<PageResponse<BidResponseDTO>> {
    const response = await adminApi.get(`${BASE_URL}/bids/customer/${customerId}?page=${page}&size=${size}`);
    return response.data.data;
  }
}; 