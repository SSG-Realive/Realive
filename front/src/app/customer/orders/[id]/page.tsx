// 주문 상세
'use client';

import { useEffect, useState } from 'react';
import { getOrderDetail, cancelOrder } from '@/service/orderService';
import { useParams, useRouter } from 'next/navigation';
import { OrderResponse } from '@/types/order';

export default function OrderDetailPage() {
    const [order, setOrder] = useState<OrderResponse | null>(null);
    const params = useParams();
    const router = useRouter();
    const orderId = Number(params?.id);

    useEffect(() => {
        const fetchDetail = async () => {
            const data = await getOrderDetail(orderId);
            setOrder(data);
        };
        fetchDetail();
    }, [orderId]);

    const handleCancel = async () => {
        if (!confirm('정말 주문을 취소하시겠습니까?')) return;
        await cancelOrder(orderId);
        alert('주문이 취소되었습니다.');
        router.push('/orders');
    };

    if (!order) return <div className="p-4">로딩 중...</div>;

    return (
        <div className="p-6">
            <h1 className="text-2xl font-bold mb-4">주문 상세</h1>
            <p><strong>주문번호:</strong> {order.orderId}</p>
            <p><strong>수령인:</strong> {order.receiverName}</p>
            <p><strong>배송지:</strong> {order.deliveryAddress}</p>
            <p><strong>총 금액:</strong> {order.totalPrice.toLocaleString()}원</p>

            <h2 className="mt-4 font-semibold">상품 목록</h2>
            <ul className="space-y-2">
                {order.orderItems.map(item => (
                    <li key={item.productId}>
                        {item.productName} - {item.price}원 x {item.quantity}
                    </li>
                ))}
            </ul>

            <button
                className="mt-6 px-4 py-2 bg-red-500 text-white rounded"
                onClick={handleCancel}
            >
                주문 취소
            </button>
        </div>
    );
}