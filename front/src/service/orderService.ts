import apiClient from '@/lib/apiClient';
import { OrderResponse, PageResponse } from '@/types/order';

/**
 * 주문 목록 조회
 */
export async function getMyOrders(page: number): Promise<PageResponse<OrderResponse>> {
    const res = await apiClient.get(`/api/customer/orders?page=${page}`);
    return res.data;
}

/**
 * 주문 상세 조회
 */
export async function getOrderDetail(orderId: number): Promise<OrderResponse> {
    const res = await apiClient.get(`/api/customer/orders/${orderId}`);
    return res.data;
}

/**
 * 주문 생성
 */
export async function createOrder(payload: {
    productId: number;
    quantity: number;
    deliveryAddress: string;
}): Promise<number> {
    const res = await apiClient.post('/api/customer/orders', payload);
    return res.data;
}

/**
 * 주문 취소
 */
export async function cancelOrder(orderId: number): Promise<void> {
    await apiClient.delete(`/api/customer/orders/${orderId}`);
}