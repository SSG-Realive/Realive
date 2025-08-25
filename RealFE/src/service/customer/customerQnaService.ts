// src/service/customer/qnaService.ts

import { customerApi } from '@/lib/apiClient';
import { AxiosError } from 'axios';
import { CustomerQnaResponse, CustomerQnaListResponse } from '@/types/customer/qna/customerQnaResponse';
import { CustomerQnaRequest } from '@/types/customer/qna/customerQnaRequest'; // ✨ 새로 만든 QnA 요청 DTO 타입 임포트

/**
 * 특정 상품에 대한 QnA 목록 조회 (고객용)
 * 백엔드 API 경로: GET /api/public/items/v1/qna/{productId}
 * (기존 코드 유지)
 */
export async function getProductQnaList(
    productId: number,
): Promise<CustomerQnaListResponse> {
    const url = `/public/items/v1/qna/${productId}`;
    console.log('=== getProductQnaList API 호출 ===');
    console.log('요청 URL:', url);

    try {
        const res = await customerApi.get<CustomerQnaResponse[]>(url);
        console.log('=== getProductQnaList API 응답 상세 ===');
        console.log('HTTP 상태:', res.status);
        console.log('응답 데이터 원본:', res.data);

        return {
            content: res.data,
            totalElements: res.data.length,
            size: res.data.length,
            totalPages: 1,
            number: 0,
            first: true,
            last: true,
            numberOfElements: res.data.length,
            empty: res.data.length === 0,
        };
    } catch (error) {
        console.error('=== getProductQnaList API 에러 ===');
        console.error('에러 상세:', error);
        console.error('요청 URL:', url);
        if (error instanceof AxiosError) {
            console.error('응답 상태 (Axios):', error.response?.status);
            console.error('응답 데이터 (Axios):', error.response?.data);
        } else if (error instanceof Error) {
            console.error('일반 Error 객체:', error.message);
        }
        throw error;
    }
}

/**
 * ✨ 새로 추가된 함수: 고객 QnA 작성 요청
 * 백엔드 API 경로: POST /api/customer/qna
 *
 * @param qnaData QnA 제목, 내용, 상품 ID, 고객 ID를 포함하는 객체
 * @returns 성공 시 QnA ID와 상품 요약 정보, 실패 시 에러
 */
export async function createCustomerQna(
    qnaData: CustomerQnaRequest, // ✨ CustomerQnaRequest 타입에 customerId가 제거되었으므로, 여기도 변경 없음
): Promise<{ qnaId: number; product: any }> {
    const url = `/customer/qna`;
    console.log('=== createCustomerQna API 호출 ===');
    console.log('요청 URL:', url);
    console.log('요청 데이터:', qnaData);

    try {
        const res = await customerApi.post<{ qnaId: number; product: any }>(url, qnaData);
        console.log('=== createCustomerQna API 응답 상세 ===');
        console.log('HTTP 상태:', res.status);
        console.log('응답 데이터:', res.data);
        return res.data;
    } catch (error) {
        console.error('=== createCustomerQna API 에러 ===');
        console.error('에러 상세:', error);
        console.error('요청 URL:', url);
        if (error instanceof AxiosError) {
            console.error('응답 상태 (Axios):', error.response?.status);
            console.error('응답 데이터 (Axios):', error.response?.data);
        } else if (error instanceof Error) {
            console.error('일반 Error 객체:', error.message);
        }
        throw error;
    }
}