'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { getSellerOrders } from '@/service/sellerOrderService';
import { SellerOrderResponse } from '@/types/sellerOrder';

export default function SellerOrderListPage() {
    const [orders, setOrders] = useState<SellerOrderResponse[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const router = useRouter();

    useEffect(() => {
        const fetchData = async () => {
            try {
                setLoading(true);
                const res = await getSellerOrders();
                setOrders(res.dtoList || []);
                setError(null);
            } catch (err) {
                console.error('주문 목록 조회 실패', err);
                setError('주문 목록을 불러오는 데 실패했습니다.');
            } finally {
                setLoading(false);
            }
        };
        fetchData();
    }, []);

    return (
        <div className="p-6">
            <h1 className="text-xl font-bold mb-4">판매자 주문 목록</h1>

            {loading ? (
                <p className="text-gray-500">로딩 중...</p>
            ) : error ? (
                <p className="text-red-500">{error}</p>
            ) : orders.length === 0 ? (
                <p className="text-gray-400">등록된 주문이 없습니다.</p>
            ) : (
                <ul className="space-y-4">
                    {orders.map((order) => (
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
            )}
        </div>
    );
}
