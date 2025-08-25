import apiClient from "@/lib/apiClient";

// 로그인 필요한 고객 경매 API
export const customerAuctionService = {
  // 로그인한 고객용 경매 목록
  async getAuctions(params?: { category?: string; status?: string; page?: number; size?: number }) {
    const response = await apiClient.get('customer/auctions', { params });
    return response.data;
  },

  // 로그인한 고객용 경매 상세
  async getAuctionDetail(auctionId: number) {
    const response = await apiClient.get(`customer/auctions/${auctionId}`);
    return response.data;
  },

  // 입찰하기 (로그인 필요)
  async placeBid(auctionId: number, bidPrice: number) {
    const response = await apiClient.post('customer/bids', {
      auctionId,
      bidPrice
    });
    return response.data;
  },

  // 내 입찰 내역
  async getMyBids(params?: { page?: number; size?: number }) {
    const response = await apiClient.get('customer/bids/my-bids', { params });
    return response.data;
  },

  // 특정 경매 입찰 내역 (로그인 필요)
  async getAuctionBids(auctionId: number, params?: { page?: number; size?: number }) {
    const response = await apiClient.get(`customer/bids/${auctionId}`, { params });
    return response.data;
  },

  // 입찰 단위 조회 (로그인 필요)
  async getTickSize(auctionId: number) {
    const response = await apiClient.get(`customer/bids/auction/${auctionId}/tick-size`);
    return response.data;
  }
};

// 로그인 불필요한 공개 경매 API
export const publicAuctionService = {
  // 공개 경매 목록 (비로그인 가능)
  async getPublicAuctions(params?: { category?: string; status?: string; page?: number; size?: number }) {
    const response = await apiClient.get('public/auctions', { params });
    return response.data;
  },

  // 공개 경매 상세 (비로그인 가능)
  async getPublicAuctionDetail(auctionId: number) {
    const response = await apiClient.get(`public/auctions/${auctionId}`);
    return response.data;
  },

  // 공개 경매 입찰 내역 (비로그인 가능)
  async getPublicAuctionBids(auctionId: number, params?: { page?: number; size?: number }) {
    const response = await apiClient.get(`public/auctions/${auctionId}/bids`, { params });
    return response.data;
  },

  // 공개 입찰 단위 조회 (비로그인 가능)
  async getPublicTickSize(auctionId: number) {
    const response = await apiClient.get(`public/auctions/${auctionId}/tick-size`);
    return response.data;
  }
};

// 기존 호환성을 위한 래퍼
export const fetchCustomerAuctions = async () => {
  return customerAuctionService.getAuctions();
};
