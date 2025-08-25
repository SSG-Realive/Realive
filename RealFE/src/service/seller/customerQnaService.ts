import { sellerApi } from '@/lib/apiClient';
import { CustomerQnaListResponse, CustomerQnaDetailResponse } from '@/types/seller/customerqna/customerQnaResponse';

// 고객 Q&A 목록 조회 (페이징)
export async function getCustomerQnaList(
    searchParams: Record<string, any> = {}
): Promise<CustomerQnaListResponse> {
    const query = new URLSearchParams(searchParams).toString();
    const url = `/seller/customer-qna/page?${query}`;
    
    console.log('=== customerQnaService API 호출 ===');
    console.log('요청 URL:', url);
    console.log('요청 파라미터:', searchParams);
    
    try {
        const res = await sellerApi.get(url);
        console.log('=== API 응답 상세 ===');
        console.log('HTTP 상태:', res.status);
        console.log('응답 헤더:', res.headers);
        console.log('res.data 원본:', res.data);
        console.log('res.data 타입:', typeof res.data);
        
    return res.data;
    } catch (error: any) {
        console.error('=== customerQnaService API 에러 ===');
        console.error('에러 상세:', error);
        console.error('요청 URL:', url);
        console.error('응답 상태:', error.response?.status);
        console.error('응답 데이터:', error.response?.data);
        throw error;
    }
}

// 고객 Q&A 상세 조회
export async function getCustomerQnaDetail(id: number): Promise<CustomerQnaDetailResponse> {
    const res = await sellerApi.get(`/seller/customer-qna/${id}`);
    return res.data;
}

// 고객 Q&A 답변 등록
export async function answerCustomerQna(id: number, answer: string): Promise<void> {
    await sellerApi.post(`/seller/customer-qna/${id}/answer`, { answer });
} 