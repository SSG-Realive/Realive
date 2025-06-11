import apiClient from '@/lib/apiClient';
import { SellerDashboardResponse } from '@/types/dashboard/sellerDashboardResponse';
import { LoginResponse } from '@/types/login/loginResponse';



// 로그인 요청
export async function login(email: string, password: string): Promise<LoginResponse> {
  const response = await apiClient.post<LoginResponse>('/seller/login', { email, password });
  return response.data;
}
//로그아웃 요청
export async function logout(): Promise<void> {
  // 1) 백엔드 /seller/logout 호출 → refreshToken 쿠키 만료
  await apiClient.post('/seller/logout');
}

// 프로필 조회
export interface SellerProfile {
  id: number;
  email: string;
  name: string;
  phone: string;
}
export async function getProfile(): Promise<SellerProfile> {
  const response = await apiClient.get<SellerProfile>('/seller/me');
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
  await apiClient.put('/seller/me', data);
}
//대시보드

export async function getDashboard() : Promise<SellerDashboardResponse> {
  const response = await apiClient.get('/seller/dashboard');
  return response.data;
}

//qna


