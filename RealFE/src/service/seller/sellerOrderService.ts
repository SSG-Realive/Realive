import { sellerApi } from '@/lib/apiClient';
import { PageResponse } from '@/types/seller/page/pageResponse';
import { PageResponseForOrder } from '@/types/seller/page/pageResponseForOrder';
import {
    SellerOrderResponse,
    SellerOrderDetailResponse,
    DeliveryStatusUpdateRequest,
} from '@/types/seller/sellerorder/sellerOrder';

// 주문 검색 조건 인터페이스 추가
export interface SellerOrderSearchParams {
    page?: number;
    size?: number;
    sort?: string;
    direction?: 'ASC' | 'DESC';
    keyword?: string;
    status?: string;
}

/**
 * 판매자 주문 목록 조회 (PageResponse 기반)
 * @param searchParams - 페이지, 정렬, 검색 필터 등
 */
export async function getSellerOrders(searchParams: SellerOrderSearchParams = {}): Promise<PageResponseForOrder<SellerOrderResponse>> {
    const params = new URLSearchParams();
    
    // 페이징 파라미터
    if (searchParams.page !== undefined) params.append('page', searchParams.page.toString());
    if (searchParams.size !== undefined) params.append('size', searchParams.size.toString());
    if (searchParams.sort) params.append('sort', searchParams.sort);
    if (searchParams.direction) params.append('direction', searchParams.direction);
    
    // 검색 조건 파라미터
    if (searchParams.keyword) params.append('keyword', searchParams.keyword);
    if (searchParams.status) params.append('status', searchParams.status);
    
    console.log('🔍 판매자 주문 목록 조회 파라미터:', Object.fromEntries(params));
    
    const res = await sellerApi.get(`/seller/orders?${params.toString()}`);
    return res.data;
}

/**
 * 주문 상세 조회 (새로운 API 사용)
 */
export async function getOrderDetail(orderId: number): Promise<SellerOrderDetailResponse> {
    const res = await sellerApi.get(`/seller/orders/${orderId}`);
    return res.data;
}

/**
 * 배송 상태 변경 (판매자 → 관리자/고객에게)
 * @param orderId - 주문 ID
 * @param updateData - 배송 상태 + 옵션: 운송장 번호, 택배사
 */
export async function updateDeliveryStatus(
    orderId: number,
    updateData: DeliveryStatusUpdateRequest
): Promise<void> {
    await sellerApi.patch(`/seller/orders/${orderId}/delivery`, updateData);
}

/**
 * 배송 취소
 * @param orderId - 주문 ID
 */
export async function cancelOrderDelivery(orderId: number): Promise<void> {
    await sellerApi.patch(`/seller/orders/${orderId}/cancel`);
}

// 주문 통계 타입 정의
export interface OrderStatistics {
    totalOrders: number;
    preparingOrders: number;
    inProgressOrders: number;
    completedOrders: number;
}

/**
 * 판매자 주문 통계 조회
 * @returns OrderStatistics - 전체/배송준비/배송중/완료 주문 수
 */
export async function getOrderStatistics(): Promise<OrderStatistics> {
    console.log('📊 주문 통계 API 호출 시작');
    const res = await sellerApi.get('/seller/orders/statistics');
    console.log('📊 주문 통계 응답:', res.data);
    return res.data;
}