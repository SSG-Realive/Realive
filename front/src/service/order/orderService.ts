// src/services/orderService.ts (수정된 버전)

import { customerApi } from '@/lib/apiClient'; // ✨ apiClient의 기본 export 대신 customerApi를 명시적으로 import
import { Page, Order, PayRequestDTO, DirectPaymentInfoDTO } from '@/types/customer/order/order';

/**
 * 특정 고객의 주문 목록을 조회하는 API 함수
 * React Query의 queryFn으로 사용됩니다.
 * @param page 조회할 페이지 번호 (0부터 시작)
 * @param size 페이지 당 아이템 수
 * @returns Promise<Page<Order>>
 */
export const getOrderList = async (page: number, size: number = 10): Promise<Page<Order>> => {
    // ✨ apiClient 대신 customerApi를 사용
    const { data } = await customerApi.get<Page<Order>>('/customer/orders', {
        params: {
            page,
            size,
            sort: 'orderedAt,desc',
        },
    });
    return data;
};

/**
 * [API] 단일 상품 바로 구매 정보 조회
 * GET /api/customer/orders/direct-payment-info
 * * @param productId 상품 ID
 * @param quantity 수량
 * @returns 상품 정보
 */
export const getDirectPaymentInfo = async (
    productId: number,
    quantity: number
): Promise<DirectPaymentInfoDTO> => {
    
    // GET 요청 시, params 옵션을 사용해 쿼리 파라미터를 전달합니다.
    // -> /api/customer/orders/direct-payment-info?productId=123&quantity=1
    const { data } = await customerApi.get<DirectPaymentInfoDTO>('/customer/orders/direct-payment-info', {
        params: {
            productId,
            quantity,
        },
    });

    return data;
};


/**
 * [API] 단일 상품 바로 구매 및 최종 결제 승인 요청
 * POST /api/customer/orders/direct-payment
 * * @param requestData 결제 승인에 필요한 모든 정보
 * @returns 생성된 주문의 ID
 */
export const processDirectPaymentApi = async (
    requestData: PayRequestDTO
): Promise<number> => { // 백엔드가 Long(숫자) 타입의 orderId를 반환

    // POST 요청 시, 두 번째 인자로 요청 본문(body) 데이터를 전달합니다.
    const { data } = await customerApi.post<number>('/customer/orders/direct-payment', requestData);

    return data;
};

export const processCartPaymentApi = async (
    requestData: PayRequestDTO
): Promise<number> => {
    // ✨ 새로운 백엔드 엔드포인트 호출
    const { data } = await customerApi.post<number>('/customer/cart/payment', requestData);
    return data;
};