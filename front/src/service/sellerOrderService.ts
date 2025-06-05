import apiClient from '@/lib/apiClient';
import { PageResponse } from '@/types/common';
import {
    SellerOrderResponse,
    SellerOrderDetailResponse,
} from '@/types/sellerOrder';

// 판매자 주문 목록 조회 API
export async function getSellerOrders(): Promise<PageResponse<SellerOrderResponse>> {
    const res = await apiClient.get('/seller/orders');
    return res.data;
}

// 주문 상세 조회 API
export async function getOrderDetail(orderId: number): Promise<SellerOrderDetailResponse> {
    const res = await apiClient.get(`/seller/orders/${orderId}`);
    return res.data;
}

// 배송 상태 변경 API
export async function updateDeliveryStatus(orderId: number, deliveryStatus: string): Promise<void> {
    await apiClient.put(`/seller/orders/${orderId}/delivery-status`, {
        deliveryStatus,
    });
}