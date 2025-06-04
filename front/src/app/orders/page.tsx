//  주문 목록
'use client';

import { useEffect, useState } from 'react';
import { getMyOrders } from '@/service/orderService';
import { OrderResponse } from '@/types/order';
import { useRouter } from 'next/navigation';

export default function OrderListPage() {
    const [orders, setOrders] = useState<OrderResponse[]>([]);
    const router = useRouter();

    useEffect(() => {
        const fetchOrders = async () => {
            const res = await getMyOrders(1);
            setOrders(res.content); // PageResponse
        };
        fetchOrders();
    }, []);

    return (
        <div className="p-6">
            <h1 className="text-2xl font-bold mb-4">주문 목록</h1>
            <ul className="space-y-4">
                {orders.map(order => (
                    <li
                        key={order.orderId}
                        className="border p-4 rounded cursor-pointer hover:bg-gray-100"
                        onClick={() => router.push(`/orders/${order.orderId}`)}
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