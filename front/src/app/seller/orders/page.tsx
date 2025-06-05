// 주문 목록 조회
'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { getSellerOrders } from '@/service/sellerOrderService';
import { SellerOrderResponse } from '@/types/sellerOrder';

export default function SellerOrderListPage() {
    const [orders, setOrders] = useState<SellerOrderResponse[]>([]);
    const router = useRouter();

    useEffect(() => {
        const fetchData = async () => {
            const res = await getSellerOrders();
            setOrders(res.content);
        };
        fetchData();
    }, []);

    return (
        <div className="p-6">
            <h1 className="text-xl font-bold mb-4">판매자 주문 목록</h1>
            <ul className="space-y-4">
                {orders.map(order => (
                    <li
                        key={order.orderId}
                        className="border p-4 rounded hover:bg-gray-100 cursor-pointer"
                        onClick={() => router.push(`/seller/orders/${order.orderId}`)}
                    >
                        <p><strong>주문번호:</strong> {order.orderId}</p>
                        <p><strong>상태:</strong> {order.orderStatus}</p>
                        <p><strong>총액:</strong> {order.totalPrice.toLocaleString()}원</p>
                    </li>
                ))}
            </ul>
        </div>
    );
}