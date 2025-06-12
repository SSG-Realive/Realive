import apiClient from '@/lib/apiClient';
import {
    SellerQnaResponse,
    SellerQnaDetailResponse,
    CreateQnaRequest,
    UpdateQnaRequest
} from '@/types/sellerQna';
import { PageResponse } from '@/types/page/pageResponse';

/**
 * 판매자 QnA 목록 조회 (페이지네이션 포함)
 */
export async function getQnaList(
    searchParams: Record<string, any> = {}
): Promise<PageResponse<SellerQnaResponse>> {
    const query = new URLSearchParams(searchParams).toString();
    const res = await apiClient.get(`/seller/qna?${query}`);
    return res.data;
}

/**
 * 판매자 QnA 단건 상세 조회
 */
export async function getQnaDetail(id: number): Promise<SellerQnaDetailResponse> {
    const res = await apiClient.get(`/seller/qna/${id}`);
    return res.data;
}

/**
 * 판매자 QnA 작성
 */
export async function createQna(data: CreateQnaRequest): Promise<void> {
    await apiClient.post('/seller/qna/new', data);
}

/**
 * 판매자 QnA 수정 (답변이 달리기 전)
 */
export async function updateQna(id: number, data: UpdateQnaRequest): Promise<void> {
    await apiClient.put(`/seller/qna/${id}`, data);
}

/**
 * 판매자 QnA 삭제
 */
export async function deleteQna(id: number): Promise<void> {
    await apiClient.delete(`/seller/qna/${id}`);
}
