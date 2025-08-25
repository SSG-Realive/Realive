import { sellerApi } from '@/lib/apiClient';
import { DeliveryStatusUpdateRequest, OrderDeliveryDetail } from '@/types/seller/sellerdelivery/sellerDelivery';

/**
 * 배송 상세 조회
 */
export async function getDeliveryDetail(orderId: number): Promise<OrderDeliveryDetail> {
    const res = await sellerApi.get(`/seller/orders/${orderId}/delivery`);
    return res.data;
}

/**
 * 배송 상태 변경
 */
export async function updateDeliveryStatus(orderId: number, dto: DeliveryStatusUpdateRequest): Promise<void> {
    await sellerApi.patch(`/seller/orders/${orderId}/delivery`, dto);

}

//배송취소 설정
export async function cancelOrderDelivery(orderId: number): Promise<void> {
    await sellerApi.patch(`/seller/orders/${orderId}/cancel`);
}

