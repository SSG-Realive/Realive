'use client';

import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import {
    getOrderDetail,
    updateDeliveryStatus,
} from '@/service/sellerOrderService';
import {
    SellerOrderDetailResponse,
    DeliveryStatus,
} from '@/types/sellerOrder';

import Header from '@/components/Header';
import SellerLayout from '@/components/layouts/SellerLayout';
import useSellerAuthGuard from '@/hooks/useSellerAuthGuard';

export default function SellerOrderDetailPage() {
    // ✅ 인증 가드 적용
    // useSellerAuthGuard();

    const params = useParams();
    const orderId = Number(params?.id);
    const router = useRouter();

    const [order, setOrder] = useState<SellerOrderDetailResponse | null>(null);
    const [newStatus, setNewStatus] = useState<DeliveryStatus>('DELIVERY_PREPARING');

    const DELIVERY_STATUSES: DeliveryStatus[] = [
        'DELIVERY_PREPARING',
        'DELIVERY_IN_PROGRESS',
        'DELIVERY_COMPLETED',
    ];

    useEffect(() => {
        const fetchData = async () => {
            try {
                const data = await getOrderDetail(orderId);
                setOrder(data);
                setNewStatus(data.deliveryStatus);
            } catch (err) {
                console.error('주문 상세 조회 실패:', err);
                alert('주문 정보를 불러오지 못했습니다.');
            }
        };
        fetchData();
    }, [orderId]);

    const handleUpdate = async () => {
        try {
            await updateDeliveryStatus(orderId, {
                deliveryStatus: newStatus,
            });
            alert('배송 상태가 변경되었습니다.');
            router.refresh();
        } catch (err) {
            console.error('배송 상태 변경 실패:', err);
            alert('배송 상태 변경에 실패했습니다.');
        }
    };

    if (!order) return <div className="p-6">로딩 중...</div>;

    return (
        <SellerLayout>
            <Header />
            <div className="p-6 max-w-3xl mx-auto">
                <h1 className="text-2xl font-bold mb-4">주문 상세</h1>
                <p><strong>수령인:</strong> {order.receiverName}</p>
                <p><strong>전화번호:</strong> {order.phone}</p>
                <p><strong>주소:</strong> {order.deliveryAddress}</p>
                <p><strong>총 가격:</strong> {order.totalPrice.toLocaleString()}원</p>
                <p><strong>결제 수단:</strong> {order.paymentType}</p>
                <p><strong>주문 상태:</strong> {order.orderStatus}</p>
                <p><strong>배송 상태:</strong></p>

                <select
                    className="border p-2 rounded mb-4"
                    value={newStatus}
                    onChange={(e) => setNewStatus(e.target.value as DeliveryStatus)}
                >
                    {DELIVERY_STATUSES.map((status) => (
                        <option key={status} value={status}>
                            {status}
                        </option>
                    ))}
                </select>

                <div>
                    <button
                        onClick={handleUpdate}
                        className="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded"
                    >
                        배송 상태 변경
                    </button>
                </div>
            </div>
        </SellerLayout>
    );
}