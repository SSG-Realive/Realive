import { AdminDashboardDTO } from '@/types/admin/admin';
import apiClient from '@/lib/apiClient';
import { ApiResponse } from '@/types/admin/api';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080/api';

const defaultHeaders = {
  'Content-Type': 'application/json',
  'Authorization': `Bearer ${typeof window !== 'undefined' ? localStorage.getItem('accessToken') : ''}`,
};

async function handleResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    const error = await response.json().catch(() => ({}));
    throw new Error(error.message || `HTTP error! status: ${response.status}`);
  }
  const data = await response.json();
  return data.data;
}

export async function getAdminDashboard(date: string, periodType: string): Promise<AdminDashboardDTO> {
  try {
    const token = typeof window !== 'undefined' ? localStorage.getItem('adminToken') : '';
    if (!token) {
      throw new Error('관리자 인증이 필요합니다.');
    }

    const response = await apiClient.get<ApiResponse<AdminDashboardDTO>>(
      `/admin/stats/main-dashboard?date=${date}&periodType=${periodType}`,
      {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        }
      }
    );

    if (!response.data || !response.data.data) {
      throw new Error('데이터를 불러오는데 실패했습니다.');
    }

    return response.data.data;
  } catch (error) {
    console.error('Failed to fetch admin dashboard:', error);
    if (error.response?.status === 403) {
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

    const response = await apiClient.get(
      `/api/admin/stats/sales-period?${params.toString()}`
    );
    return response.data;
  } catch (error) {
    console.error('Failed to fetch sales statistics:', error);
    throw error;
  }
}

export async function getAuctionStatistics(startDate: string, endDate: string) {
  try {
    const response = await apiClient.get(
      `/api/admin/stats/auctions-period?startDate=${startDate}&endDate=${endDate}`
    );
    return response.data;
  } catch (error) {
    console.error('Failed to fetch auction statistics:', error);
    throw error;
  }
}

export async function getMemberStatistics(startDate: string, endDate: string) {
  try {
    const response = await apiClient.get(
      `/api/admin/stats/members-period?startDate=${startDate}&endDate=${endDate}`
    );
    return response.data;
  } catch (error) {
    console.error('Failed to fetch member statistics:', error);
    throw error;
  }
}

export async function getReviewStatistics(startDate: string, endDate: string) {
  try {
    const response = await apiClient.get(
      `/api/admin/stats/reviews-period?startDate=${startDate}&endDate=${endDate}`
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
    const response = await apiClient.post<AdminLoginResponse>('/admin/login', {
      email,
      password
    });
    
    // 로그인 성공 시 토큰 저장
    localStorage.setItem('adminToken', response.data.accessToken);
    localStorage.setItem('refreshToken', response.data.refreshToken);
    
    return response.data;
  } catch (error) {
    console.error('Failed to login:', error);
    throw error;
  }
} 