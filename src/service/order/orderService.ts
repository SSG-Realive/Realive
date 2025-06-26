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

/**
 * [API] 주문 내역 삭제
 * DELETE /api/customer/orders
 * @param orderId 삭제할 주문의 ID
 * @returns Promise<void>
 */
export const deleteOrder = async (orderId: number): Promise<void> => {
    try {
        const { status } = await customerApi.delete<void>('/customer/orders', {
            data: { orderId }
        });

        if (status !== 204 && status !== 200) {
            throw new Error(`주문 삭제에 실패했습니다. (HTTP 상태 코드: ${status})`);
        }
    } catch (error: any) { // ✨ error 타입을 any로 지정하여 유연하게 처리
        // error 객체에 response 속성이 있다면, Axios에서 발생한 HTTP 에러로 간주합니다.
        if (error.response) {
            // 서버에서 보낸 에러 응답 데이터를 확인합니다.
            // Spring Boot의 기본 예외 응답은 'message' 필드에 에러 메시지를 담습니다.
            const serverResponseData = error.response.data;

            // 'message' 필드가 존재하고 문자열이라면 해당 메시지를 사용합니다.
            if (serverResponseData?.message && typeof serverResponseData.message === 'string') {
                throw new Error(serverResponseData.message);
            } else if (error.response.status) {
                // 특정 HTTP 상태 코드에 따른 일반적인 메시지
                if (error.response.status === 400) {
                    throw new Error('잘못된 요청입니다. 입력값을 확인해주세요.');
                } else if (error.response.status === 403) {
                    throw new Error('삭제 권한이 없습니다.');
                } else if (error.response.status === 404) {
                    throw new Error('해당 주문을 찾을 수 없습니다.');
                } else if (error.response.status === 409) {
                    throw new Error('데이터 충돌이 발생했습니다. 다시 시도해주세요.');
                } else if (error.response.status >= 500) {
                    // 5xx 서버 에러일 경우, 서버에서 보낸 메시지가 없으면 일반적인 서버 오류 메시지
                    throw new Error('서버에 문제가 발생했습니다. 잠시 후 다시 시도해주세요.');
                }
            }
            // 그 외 알 수 없는 형태의 서버 응답이라면 일반적인 실패 메시지
            throw new Error(`주문 삭제 실패: ${error.response.statusText || '알 수 없는 오류'}`);

        }
        // response 객체가 없는 경우 (네트워크 오류, 요청 취소 등)
        throw new Error(`네트워크 오류가 발생했습니다: ${error.message || '알 수 없는 오류'}`);
    }
};