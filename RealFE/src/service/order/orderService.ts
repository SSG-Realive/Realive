// src/services/orderService.ts

import { customerApi } from '@/lib/apiClient';
import {
    Page,
    Order,
    PayRequestDTO,
    DirectPaymentInfoDTO,
} from '@/types/customer/order/order';

/**
 * 특정 고객의 주문 목록을 조회하는 API 함수
 * React Query의 queryFn으로 사용됩니다.
 * @param page 조회할 페이지 번호 (0부터 시작)
 * @param size 페이지 당 아이템 수
 * @returns Promise<Page<Order>>
 */
export const getOrderList = async (
    page: number,
    size: number = 10
): Promise<Page<Order>> => {
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
 * @param productId 상품 ID
 * @param quantity 수량
 * @returns 상품 정보
 */
export const getDirectPaymentInfo = async (
    productId: number,
    quantity: number
): Promise<DirectPaymentInfoDTO> => {
    const { data } = await customerApi.get<DirectPaymentInfoDTO>(
        '/customer/orders/direct-payment-info',
        {
            params: {
                productId,
                quantity,
            },
        }
    );
    return data;
};

/**
 * [API] 단일 상품 바로 구매 및 최종 결제 승인 요청
 * POST /api/customer/orders/direct-payment
 * @param requestData 결제 승인에 필요한 모든 정보
 * @returns 생성된 주문의 ID
 */
export const processDirectPaymentApi = async (
    requestData: PayRequestDTO
): Promise<number> => {
    const { data } = await customerApi.post<number>(
        '/customer/orders/direct-payment',
        requestData
    );
    return data;
};

/**
 * [API] 장바구니 결제 요청
 * POST /api/customer/cart/payment
 * @param requestData 결제 정보
 * @returns 생성된 주문의 ID
 */
export const processCartPaymentApi = async (
    requestData: PayRequestDTO
): Promise<number> => {
    const { data } = await customerApi.post<number>(
        '/customer/cart/payment',
        requestData
    );
    return data;
};

/**
 * [API] 주문 내역 삭제
 * DELETE /api/customer/orders
 * @param orderId 삭제할 주문의 ID
 * @returns Promise<void>
 */
export const deleteOrder = async (orderId: number): Promise<void> => {
    try {
        const { status } = await customerApi.delete<void>('/customer/orders', {
            data: { orderId },
        });

        if (status !== 204 && status !== 200) {
            throw new Error(
                `주문 삭제에 실패했습니다. (HTTP 상태 코드: ${status})`
            );
        }
    } catch (error: any) {
        if (error.response) {
            const serverResponseData = error.response.data;

            if (
                serverResponseData?.message &&
                typeof serverResponseData.message === 'string'
            ) {
                throw new Error(serverResponseData.message);
            } else if (error.response.status) {
                if (error.response.status === 400) {
                    throw new Error('잘못된 요청입니다. 입력값을 확인해주세요.');
                } else if (error.response.status === 403) {
                    throw new Error('삭제 권한이 없습니다.');
                } else if (error.response.status === 404) {
                    throw new Error('해당 주문을 찾을 수 없습니다.');
                } else if (error.response.status === 409) {
                    throw new Error('데이터 충돌이 발생했습니다. 다시 시도해주세요.');
                } else if (error.response.status >= 500) {
                    throw new Error('서버에 문제가 발생했습니다. 잠시 후 다시 시도해주세요.');
                }
            }
            throw new Error(
                `주문 삭제 실패: ${error.response.statusText || '알 수 없는 오류'}`
            );
        }
        throw new Error(
            `네트워크 오류가 발생했습니다: ${error.message || '알 수 없는 오류'}`
        );
    }
};

/**
 * [API] 최근 주문 1건 조회 (마이페이지 미리보기용)
 * GET /api/customer/orders/preview
 * @returns 최근 주문 정보
 */
export const getRecentOrder = async (): Promise<Order> => {
    const { data } = await customerApi.get<Order>('/customer/orders/preview');
    return data;
};
