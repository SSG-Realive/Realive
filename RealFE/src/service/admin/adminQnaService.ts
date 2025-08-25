import { adminApi } from '@/app/lib/axios';

export interface AdminSellerQnaResponse {
  id: number;
  title: string;
  content: string;
  answer: string | null;
  isAnswered: boolean;
  createdAt: string;
  updatedAt: string;
  answeredAt: string | null;
  sellerId: number;
  sellerName: string;
  sellerEmail: string;
}

export interface AdminSellerQnaListResponse {
  content: AdminSellerQnaResponse[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  numberOfElements: number;
  empty: boolean;
}

export interface AdminSellerQnaStatistics {
  totalCount: number;
  answeredCount: number;
  unansweredCount: number;
  answerRate: number;
}

export interface AdminSellerQnaAnswerRequest {
  answer: string;
}

// 판매자 Q&A 목록 조회
export async function getSellerQnaList(params: {
  page?: number;
  size?: number;
  isAnswered?: boolean;
  keyword?: string;
  sellerId?: number;
}): Promise<AdminSellerQnaListResponse> {
  const queryParams = new URLSearchParams();
  if (params.page !== undefined) queryParams.append('page', params.page.toString());
  if (params.size !== undefined) queryParams.append('size', params.size.toString());
  if (params.isAnswered !== undefined) queryParams.append('isAnswered', params.isAnswered.toString());
  if (params.keyword) queryParams.append('keyword', params.keyword);
  if (params.sellerId) queryParams.append('sellerId', params.sellerId.toString());

  const response = await adminApi.get(`/admin/seller-qna?${queryParams.toString()}`);
  return response.data;
}

// 판매자 Q&A 상세 조회
export async function getSellerQnaDetail(id: number): Promise<AdminSellerQnaResponse> {
  const response = await adminApi.get(`/admin/seller-qna/${id}`);
  return response.data;
}

// 판매자 Q&A 답변 등록/수정
export async function answerSellerQna(id: number, answer: string): Promise<void> {
  await adminApi.post(`/admin/seller-qna/${id}/answer`, { answer });
}

// 판매자 Q&A 삭제
export async function deleteSellerQna(id: number): Promise<void> {
  await adminApi.delete(`/admin/seller-qna/${id}`);
}

// 통계 조회
export async function getSellerQnaStatistics(): Promise<AdminSellerQnaStatistics> {
  const response = await adminApi.get('/admin/seller-qna/statistics');
  return response.data;
} 