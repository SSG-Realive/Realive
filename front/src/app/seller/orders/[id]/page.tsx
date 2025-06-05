//주문 상세 및 배송 상태 변경
'use client';

import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import {
    getOrderDetail,
    updateDeliveryStatus,
} from '@/service/sellerOrderService';
import { SellerOrderDetailResponse } from '@/types/sellerOrder';

export default function SellerOrderDetailPage() {
    const params = useParams();
    const orderId = Number(params?.id);
    const router = useRouter();
    const [order, setOrder] = useState<SellerOrderDetailResponse | null>(null);
    const [newStatus, setNewStatus] = useState<string>('');

    useEffect(() => {
        const fetchData = async () => {
            const data = await getOrderDetail(orderId);
            setOrder(data);
            setNewStatus(data.deliveryStatus);
        };
        fetchData();
    }, [orderId]);

    const handleUpdate = async () => {
        await updateDeliveryStatus(orderId, newStatus);
        alert('배송 상태가 변경되었습니다.');
        router.refresh();
    };

    if (!order) return <div className="p-6">로딩 중...</div>;

    return (
        <div className="p-6 max-w-3xl mx-auto">
            <h1 className="text-xl font-bold mb-4">주문 상세</h1>
            <p><strong>수령인:</strong> {order.receiverName}</p>
            <p><strong>전화번호:</strong> {order.phone}</p>
            <p><strong>배송지:</strong> {order.deliveryAddress}</p>
            <p><strong>배송비:</strong> {order.deliveryFee.toLocaleString()}원</p>

            <h2 className="mt-6 font-bold">상품 목록</h2>
            <ul className="list-disc ml-5">
                {order.items.map(item => (
                    <li key={item.productId}>
                        {item.productName} ({item.quantity}개) - {item.price.toLocaleString()}원
                    </li>
                ))}
            </ul>

            <div className="mt-6">
                <label className="block mb-2 font-semibold">배송 상태 변경</label>
                <select
                    value={newStatus}
                    onChange={(e) => setNewStatus(e.target.value)}
                    className="border p-2"
                >
                    <option value="배송준비중">배송준비중</option>
                    <option value="배송중">배송중</option>
                    <option value="배송완료">배송완료</option>
                </select>

                <button
                    className="ml-4 px-4 py-2 bg-blue-600 text-white rounded"
                    onClick={handleUpdate}
                >
                    상태 변경
                </button>
            </div>
        </div>
    );
}