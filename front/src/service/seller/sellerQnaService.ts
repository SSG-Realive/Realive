
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
    const res = await sellerApi.get(`/seller/qna?${query}`);
    return res.data;
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
    await sellerApi.post('/seller/qna/new', data);
}

/**
 * 판매자 QnA 수정 (답변이 달리기 전)
 */
export async function updateQna(id: number, data: SellerQnaUpdateRequest): Promise<void> {
    await sellerApi.put(`/seller/qna/${id}/edit`, data);
}

/**
 * 판매자 QnA 삭제
 */
export async function deleteQna(id: number): Promise<void> {
    await sellerApi.patch(`/seller/qna/${id}/edit`);
}