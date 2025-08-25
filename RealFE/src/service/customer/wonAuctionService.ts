// src/service/customer/wonAuctionService.ts
 // 기존에 사용하시던 axios 인스턴스
import { SpringPage } from '@/types/common/springPage';
import { WonAuction } from '@/types/customer/auctions';
import apiClient from "@/lib/apiClient";
// API 요청 시 사용할 파라미터 타입
export interface GetWonAuctionsParams {
  page: number;
  size: number;
}

/**
 * 인증된 사용자가 낙찰받은 경매 목록을 가져옵니다. (페이지네이션)
 * API: GET /api/customer/auction-wins
 */
const getWonAuctions = async (params: GetWonAuctionsParams): Promise<SpringPage<WonAuction>> => {
  try {
    // 실제 API 호출
    const response = await apiClient.get('/customer/auction-wins', {
      params,
    });
    
    // 백엔드의 ApiResponse<SpringPage<...>> 구조에 맞춰 실제 데이터인 response.data.data를 반환
    return response.data.data;
  } catch (error) {
    console.error('낙찰 경매 목록 조회 실패:', error);
    throw error;
  }
};

export const wonAuctionService = {
  getWonAuctions,
};