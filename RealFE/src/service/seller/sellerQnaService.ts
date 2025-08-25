
import {
    SellerCreateQnaRequest,
    SellerQnaUpdateRequest,

} from '@/types/seller/sellerqna/sellerQnaRequest';
import{
    SellerQnaDetailResponse,
    SellerQnaListResponse,
    SellerQnaResponse
} from '@/types/seller/sellerqna/sellerQnaResponse';
import { PageResponse } from '@/types/seller/page/pageResponse';
import { sellerApi } from '@/lib/apiClient';

/**
 * 판매자 QnA 목록 조회 (페이지네이션 포함)
 */
export async function getQnaList(
    searchParams: Record<string, any> = {}
): Promise<SellerQnaListResponse> {
    const query = new URLSearchParams(searchParams).toString();
    const url = `/seller/qna?${query}`;
    
    console.log('=== SellerQna 목록 조회 API 호출 ===');
    console.log('요청 URL:', url);
    console.log('요청 파라미터:', searchParams);
    
    try {
        const res = await sellerApi.get(url);
        console.log('=== API 응답 상세 ===');
        console.log('HTTP 상태:', res.status);
        console.log('res.data 원본:', res.data);
        console.log('res.data.content 첫 번째 항목:', res.data?.content?.[0]);
        
    return res.data;
    } catch (error: any) {
        console.error('=== SellerQna 목록 조회 API 에러 ===');
        console.error('에러 상세:', error);
        console.error('요청 URL:', url);
        console.error('응답 상태:', error.response?.status);
        console.error('응답 데이터:', error.response?.data);
        throw error;
    }
}

/**
 * 판매자 QnA 단건 상세 조회
 */
export async function getQnaDetail(id: number): Promise<SellerQnaDetailResponse> {
    const res = await sellerApi.get(`/seller/qna/${id}`);
    return res.data;
}

/**
 * 판매자 QnA 작성
 */
export async function createQna(data: SellerCreateQnaRequest): Promise<void> {
    console.log('=== SellerQna 생성 API 호출 ===');
    console.log('요청 데이터:', data);
    console.log('요청 URL: /seller/qna/new');
    
    try {
        const response = await sellerApi.post('/seller/qna/new', data);
        console.log('응답 성공:', response.status, response.data);
    } catch (error: any) {
        console.error('=== SellerQna 생성 API 에러 ===');
        console.error('에러 상세:', error);
        console.error('응답 상태:', error.response?.status);
        console.error('응답 데이터:', error.response?.data);
        throw error;
    }
}

/**
 * 판매자 QnA 수정 (답변이 달리기 전)
 */
export async function updateQna(id: number, data: SellerQnaUpdateRequest): Promise<void> {
    await sellerApi.put(`/seller/qna/${id}/edit`, data);
}

/**
 * 판매자 QnA 삭제 (논리적 삭제 - isActive를 false로 변경)
 */
export async function deleteQna(id: number): Promise<void> {
    await sellerApi.patch(`/seller/qna/${id}/delete`);
}