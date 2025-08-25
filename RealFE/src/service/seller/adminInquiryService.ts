import { sellerApi } from '@/lib/apiClient';

// 판매자 -> 관리자 문의 관련 타입 정의 (백엔드 DTO에 맞춤)
export interface AdminInquiryRequest {
  title: string;
  content: string;
}

export interface AdminInquiryResponse {
  id: number;
  sellerId?: number;
  sellerName?: string;
  category?: string;
  title: string;
  content: string;
  priority?: string;
  status?: 'PENDING' | 'IN_PROGRESS' | 'ANSWERED' | 'CLOSED';
  isAnswered: boolean;
  answer?: string;
  answeredBy?: number;
  answeredAt?: string;
  createdAt: string;
  updatedAt: string;
}

export interface AdminInquiryListResponse {
  content: AdminInquiryResponse[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  numberOfElements: number;
  empty: boolean;
}

// 새로운 통계 타입 정의
export interface AdminInquiryStatistics {
  totalCount: number;      // 전체 문의 수
  unansweredCount: number; // 미답변 수
  answeredCount: number;   // 답변완료 수
  answerRate: number;      // 답변률 (%)
}

/**
 * 관리자 문의 등록
 */
export async function createAdminInquiry(data: AdminInquiryRequest): Promise<void> {
  await sellerApi.post('/seller/adminqna/new', data);
}

/**
 * 관리자 문의 목록 조회
 */
export async function getAdminInquiryList(
  searchParams: Record<string, any> = {}
): Promise<AdminInquiryListResponse> {
  const query = new URLSearchParams(searchParams).toString();
  const res = await sellerApi.get(`/seller/adminqna?${query}`);
  return res.data;
}

/**
 * 관리자 문의 상세 조회
 */
export async function getAdminInquiryDetail(id: number): Promise<AdminInquiryResponse> {
  const res = await sellerApi.get(`/seller/adminqna/${id}`);
  return res.data;
}

/**
 * 관리자 문의 수정 (답변 전)
 */
export async function updateAdminInquiry(id: number, data: AdminInquiryRequest): Promise<void> {
  await sellerApi.put(`/seller/adminqna/${id}/edit`, data);
}

/**
 * 관리자 문의 삭제
 */
export async function deleteAdminInquiry(id: number): Promise<void> {
  await sellerApi.patch(`/seller/adminqna/${id}/edit`);
}

/**
 * 관리자 문의 통계 조회
 */
export async function getAdminInquiryStatistics(): Promise<AdminInquiryStatistics> {
  const res = await sellerApi.get('/seller/adminqna/statistics');
  return res.data;
} 