import { sellerApi } from '@/lib/apiClient';
import { CustomerQnaListResponse, CustomerQnaDetailResponse } from '@/types/seller/customerqna/customerQnaResponse';

// 고객 Q&A 목록 조회 (페이징)
export async function getCustomerQnaList(
    searchParams: Record<string, any> = {}
): Promise<CustomerQnaListResponse> {
    const query = new URLSearchParams(searchParams).toString();
    const res = await sellerApi.get(`/seller/customer-qna/page?${query}`);
    return res.data;
}

// 고객 Q&A 상세 조회
export async function getCustomerQnaDetail(id: number): Promise<{qna: CustomerQnaDetailResponse}> {
    const res = await sellerApi.get(`/seller/customer-qna/${id}`);
    return res.data;
}

// 고객 Q&A 답변 등록
export async function answerCustomerQna(id: number, answer: string): Promise<void> {
    await sellerApi.post(`/seller/customer-qna/${id}/answer`, { answer });
} 