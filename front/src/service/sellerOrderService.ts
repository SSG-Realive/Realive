import apiClient from '@/lib/apiClient';
import { PageResponse } from '@/types/page/pageResponse';
import {
    SellerOrderResponse,
    SellerOrderDetailResponse,
    DeliveryStatusUpdateRequest, // 🔹 추가된 타입
} from '@/types/sellerOrder';

/**
 * 판매자 주문 목록 조회 (PageResponse 기반)
 * @param searchParams - 페이지, 정렬, 검색 필터 등
 */
export async function getSellerOrders(
    searchParams: Record<string, any> = {}
): Promise<PageResponse<SellerOrderResponse>> {
    const query = new URLSearchParams(searchParams).toString();
    const res = await apiClient.get(`/seller/orders?${query}`);
    return res.data;
}

/**
 * 주문 상세 조회
 */
export async function getOrderDetail(orderId: number): Promise<SellerOrderDetailResponse> {
    const res = await apiClient.get(`/seller/orders/${orderId}`);
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
    await apiClient.patch(`/seller/orders/${orderId}/delivery`, updateData);
}