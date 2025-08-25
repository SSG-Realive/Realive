import { sellerApi } from '@/lib/apiClient';
import { useSellerAuthStore } from '@/store/seller/useSellerAuthStore';
import { SellerDashboardResponse, SellerSalesStatsDTO, DailySalesDTO, MonthlySalesDTO } from '@/types/seller/dashboard/sellerDashboardResponse';
import { LoginResponse } from '@/types/seller/login/loginResponse';



// 로그인 요청
export const login = async (email: string, password: string): Promise<LoginResponse> => {
  const response = await sellerApi.post('/seller/login', { email, password });
  return response.data;
}


//로그아웃 요청
export async function logout(): Promise<void> {
  await sellerApi.post('/seller/logout'); // 서버 쿠키 제거
  useSellerAuthStore.getState().logout(); // 상태도 초기화
}

// 프로필 조회
export interface SellerProfile {
  id: number;
  email: string;
  name: string;
  phone: string;
}
export async function getProfile(): Promise<SellerProfile> {
  const response = await sellerApi.get<SellerProfile>('/seller/me');
  return response.data;
}

// 프로필 수정
export interface SellerUpdateRequest {
  name: string;
  phone: string;
  password?: string;
}
// 백엔드가 PUT 으로 받으니 여기서도 PUT으로 바꿔야 합니다.
export async function updateProfile(data: SellerUpdateRequest): Promise<void> {
  await sellerApi.put('/seller/me', data);
}
//대시보드

export async function getDashboard() : Promise<SellerDashboardResponse> {
  const response = await sellerApi.get('/seller/dashboard');
  return response.data;
}

// 새로운 판매 통계 API 함수들
export async function getSalesStatistics(startDate: string, endDate: string): Promise<SellerSalesStatsDTO> {
  const response = await sellerApi.get(`/seller/dashboard/sales-stats?startDate=${startDate}&endDate=${endDate}`);
  return response.data;
}

export async function getDailySalesTrend(startDate: string, endDate: string): Promise<DailySalesDTO[]> {
  const response = await sellerApi.get(`/seller/dashboard/daily-sales-trend?startDate=${startDate}&endDate=${endDate}`);
  return response.data;
}

export async function getMonthlySalesTrend(startDate: string, endDate: string): Promise<MonthlySalesDTO[]> {
  const response = await sellerApi.get(`/seller/dashboard/monthly-sales-trend?startDate=${startDate}&endDate=${endDate}`);
  return response.data;
}

//qna

