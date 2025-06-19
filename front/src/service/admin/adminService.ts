import { AdminDashboardDTO } from '@/types/admin/admin';
import { adminApi } from '@/app/lib/axios';
import { ApiResponse } from '@/types/admin/api';


function isApiError(error: unknown): error is { response?: { status?: number, data?: { message?: string } } } {
  return (
      typeof error === 'object' &&
      error !== null &&
      'response' in error &&
      typeof (error as any).response === 'object'
  );
}

export async function getAdminDashboard(date: string, periodType: string): Promise<AdminDashboardDTO> {
  try {
    // adminApi가 자동으로 토큰을 zustand에서 읽어옵니다.
    const response = await adminApi.get<ApiResponse<AdminDashboardDTO>>(
        `/admin/stats/main-dashboard?date=${date}&periodType=${periodType}`
    );

    if (!response.data || !response.data.data) {
      throw new Error('데이터를 불러오는데 실패했습니다.');
    }

    return response.data.data;
  } catch (error) {
    console.error('Failed to fetch admin dashboard:', error);
    // 타입 가드 사용
    if (isApiError(error) && error.response?.status === 403) {
      throw new Error('관리자 인증이 필요합니다.');
    }
    throw error;
  }
}

export async function getSalesStatistics(
    startDate: string,
    endDate: string,
    sellerId?: number,
    sortBy?: string
) {
  try {
    const params = new URLSearchParams({
      startDate,
      endDate,
      ...(sellerId && { sellerId: sellerId.toString() }),
      ...(sortBy && { sortBy }),
    });

    const response = await adminApi.get(
        `/admin/stats/sales-period?${params.toString()}`
    );
    return response.data;
  } catch (error) {
    console.error('Failed to fetch sales statistics:', error);
    throw error;
  }
}

export async function getAuctionStatistics(startDate: string, endDate: string) {
  try {
    const response = await adminApi.get(
        `/admin/stats/auctions-period?startDate=${startDate}&endDate=${endDate}`
    );
    return response.data;
  } catch (error) {
    console.error('Failed to fetch auction statistics:', error);
    throw error;
  }
}

export async function getMemberStatistics(startDate: string, endDate: string) {
  try {
    const response = await adminApi.get(
        `/admin/stats/members-period?startDate=${startDate}&endDate=${endDate}`
    );
    return response.data;
  } catch (error) {
    console.error('Failed to fetch member statistics:', error);
    throw error;
  }
}

export async function getReviewStatistics(startDate: string, endDate: string) {
  try {
    const response = await adminApi.get(
        `/admin/stats/reviews-period?startDate=${startDate}&endDate=${endDate}`
    );
    return response.data;
  } catch (error) {
    console.error('Failed to fetch review statistics:', error);
    throw error;
  }
}

interface AdminLoginResponse {
  accessToken: string;
  refreshToken: string;
}

export async function adminLogin(email: string, password: string): Promise<AdminLoginResponse> {
  try {
    const response = await adminApi.post<AdminLoginResponse>('/admin/login', {
      email,
      password
    });

    // 로그인 성공 시 store setState(getState 사용 권장, page.tsx에서 setState 처리)
    // 토큰 자체를 이 함수에서 직접 저장할 필요가 없으므로 제거 (외부에서 setState)
    return response.data;
  } catch (error) {
    console.error('Failed to login:', error);
    throw error;
  }
}