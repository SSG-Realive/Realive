'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { getSellerOrders } from '@/service/sellerOrderService';
import { SellerOrderResponse } from '@/types/sellerOrder';
import Header from '@/components/Header';
import SellerLayout from '@/components/layouts/SellerLayout';
import useSellerAuthGuard from '@/hooks/useSellerAuthGuard';

export default function SellerOrderListPage() {

     // useSellerAuthGuard();

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
        <SellerLayout>
            <Header />
            <div className="p-6 max-w-4xl mx-auto">
                <h1 className="text-2xl font-bold mb-4">판매자 주문 목록</h1>

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
                                className="bg-white shadow-md rounded-lg p-4 cursor-pointer hover:bg-gray-50"
                                onClick={() => router.push(`/seller/orders/${order.orderId}`)}
                            >
                                <p>주문 ID: {order.orderId}</p>
                                <p>총 가격: {order.totalPrice.toLocaleString()}원</p>
                                <p>상태: {order.orderStatus}</p>
                            </li>
                        ))}
                    </ul>
                )}
            </div>
        </SellerLayout>
    );
}