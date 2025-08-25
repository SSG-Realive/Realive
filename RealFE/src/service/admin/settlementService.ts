import { adminApi } from '@/lib/apiClient';
import {
  AdminPayoutResponse,
  AdminPayoutDetailResponse,
  AdminSettlementStatisticsResponse,
  DailyPayoutSummaryResponse,
  MonthlyPayoutSummaryResponse,
  MonthlyPayoutDetailResponse,
  AdminSettlementPageResponse,
  AdminPayoutSearchCondition
} from '@/types/admin/settlement';

// 관리자 정산관리 API 서비스
export const adminSettlementService = {
  // 정산 목록 조회 (관리자용)
  getPayoutList: async (
    page: number = 0,
    size: number = 10,
    searchCondition?: AdminPayoutSearchCondition
  ): Promise<AdminSettlementPageResponse<AdminPayoutResponse>> => {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
      ...(searchCondition?.sellerName && { sellerName: searchCondition.sellerName }),
      ...(searchCondition?.periodStart && { periodStart: searchCondition.periodStart }),
      ...(searchCondition?.periodEnd && { periodEnd: searchCondition.periodEnd })
    });

    const response = await adminApi.get(`/admin/settlements?${params}`);
    return response.data;
  },

  // 정산 상세 조회 (관리자용)
  getPayoutDetail: async (payoutId: number): Promise<AdminPayoutDetailResponse> => {
    const response = await adminApi.get(`/admin/settlements/${payoutId}`);
    return response.data;
  },

  // 정산 통계 조회 (관리자용)
  getSettlementStatistics: async (): Promise<AdminSettlementStatisticsResponse> => {
    const response = await adminApi.get('/admin/settlements/statistics');
    return response.data;
  },

  // 일별 정산 요약 조회 (매출 추이 그래프용)
  getDailyPayoutSummary: async (
    startDate: string,
    endDate: string
  ): Promise<DailyPayoutSummaryResponse[]> => {
    const params = new URLSearchParams({
      startDate,
      endDate
    });

    const response = await adminApi.get(`/admin/settlements/trend/daily?${params}`);
    return response.data;
  },

  // 월별 정산 요약 조회 (월별 매출 추이 그래프용)
  getMonthlyPayoutSummary: async (
    startDate: string,
    endDate: string
  ): Promise<MonthlyPayoutSummaryResponse[]> => {
    const params = new URLSearchParams({
      startDate,
      endDate
    });

    const response = await adminApi.get(`/admin/settlements/trend/monthly?${params}`);
    return response.data;
  },

  // 월별 정산 상세 조회
  getMonthlyPayoutDetail: async (yearMonth: string): Promise<MonthlyPayoutDetailResponse> => {
    const response = await adminApi.get(`/admin/settlements/monthly-detail/${yearMonth}`);
    return response.data;
  }
}; 