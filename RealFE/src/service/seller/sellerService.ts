import { sellerApi } from '@/lib/apiClient';
import { useSellerAuthStore } from '@/store/seller/useSellerAuthStore';
import { SellerDashboardResponse, SellerSalesStatsDTO, DailySalesDTO, MonthlySalesDTO } from '@/types/seller/dashboard/sellerDashboardResponse';
import { LoginResponse } from '@/types/seller/login/loginResponse';
import {publicSellerInfoResponseDTO} from "@/types/seller/publicSellerInfoResponseDTO";
import {ReviewResponseDTO} from "@/types/reviews/reviewResponseDTO";
import {ReviewListResponseDTO} from "@/types/reviews/reviewListResponseDTO";
import {ProductListDTO} from "@/types/seller/product/product";

// 새로운 통계 타입 정의
export interface TodayStatsDTO {
  todayOrderCount: number;
  todayRevenue: number;
  date: string;
}

export interface CurrentMonthStatsDTO {
  currentMonthOrderCount: number;
  currentMonthRevenue: number;
  yearMonth: string;
}

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
  newPassword?: string;
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

// 새로운 API 함수들 추가
export async function getTodayStats(): Promise<TodayStatsDTO> {
  const response = await sellerApi.get('/seller/dashboard/today-stats');
  return response.data;
}

export async function getCurrentMonthStats(): Promise<CurrentMonthStatsDTO> {
  const response = await sellerApi.get('/seller/dashboard/current-month-stats');
  return response.data;
}

// 고객(비회원포함)의 판매자 전체 목록 또는 기간별 조회 (기존 함수 유지)
export async function getSellerPublicInfoList(startDate: string, endDate: string): Promise<publicSellerInfoResponseDTO[]> {
  const response = await sellerApi.get(`/public/seller`, {
    params: { startDate, endDate } // 쿼리 파라미터로 전달
  });
  return response.data; // 목록 형태일 수 있습니다. DTO가 단일 객체라면 조정 필요
}


// sellerId 기반으로 API 요청
export async function getSellerPublicInfoBySellerId(sellerId: number): Promise<publicSellerInfoResponseDTO | null> {
  try {
    const response = await sellerApi.get(`/public/seller/${sellerId}`);
    return response.data;
  } catch (error: any) {
    if (error.response?.status === 404) {
      console.warn(`판매자 ID ${sellerId}의 공개 정보를 찾을 수 없습니다.`);
      return null;
    }
    throw error;
  }
}


// 특정 판매자의 리뷰 조회 (수정된 함수)
export async function getSellerReviews(
  sellerId: number,
  page: number = 0,
  size: number = 5,
  sort: string = "createdAt",
  direction: string = "DESC"
): Promise<{ reviews: ReviewResponseDTO[]; hasMore: boolean }> {
  try {
    const response = await sellerApi.get(`/public/seller/${sellerId}/reviews`, {
      params: { page: page + 1, size, sort, direction },
    });

    const data = response.data;
    const reviews = data.dtoList || []; // ✅ dtoList로 변경
    const hasMore = (page + 1) * size < data.total;

    return { reviews, hasMore };
  } catch (error) {
    console.error(`판매자 리뷰 가져오기 오류 (ID: ${sellerId}, 페이지: ${page}):`, error);
    return { reviews: [], hasMore: false };
  }
}



// 특정 판매자의 상품 조회 (추가 또는 기존 함수 수정)
export async function getSellerProducts(
    sellerId: number,
    page: number = 0,
    size: number = 10,
    orderBy: string = "createdAt",
    order: string = "desc"
): Promise<{ products: ProductListDTO[]; hasMore: boolean }> {
    try {
        const response = await sellerApi.get(`/public/seller/${sellerId}/products`, {
            params: { page: page + 1, size, orderBy, order }
        });

        const data = response.data as Partial<{
            dtoList: ProductListDTO[];
            total: number;
            pageCount: number;
        }>;

        const products: ProductListDTO[] = Array.isArray(data.dtoList) ? data.dtoList : [];
        const hasMore: boolean = ((page + 1) * size) < (data.total ?? 0);
        console.log("📦 상품 API 응답 데이터:", response.data);

        return { products, hasMore };
    } catch (error) {
        console.error(`판매자 상품 가져오기 오류 (ID: ${sellerId}, 페이지: ${page}):`, error);
        return { products: [], hasMore: false };
    }
}



