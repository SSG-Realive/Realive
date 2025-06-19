'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { getSellerOrders } from '@/service/seller/sellerOrderService';
import { SellerOrderResponse } from '@/types/seller/sellerorder/sellerOrder';
import SellerHeader from '@/components/seller/SellerHeader';
import SellerLayout from '@/components/layouts/SellerLayout';
import useSellerAuthGuard from '@/hooks/useSellerAuthGuard';
import { PageResponseForOrder } from '@/types/seller/page/pageResponseForOrder';

export default function SellerOrderListPage() {
  const checking = useSellerAuthGuard(); // ✅ 인증 확인
  const [orders, setOrders] = useState<SellerOrderResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const router = useRouter();

  useEffect(() => {
    if (checking) return; // ✅ 인증 확인 중이면 아무것도 하지 않음

    const fetchData = async () => {
      try {
        setLoading(true);
        const res: PageResponseForOrder<SellerOrderResponse> = await getSellerOrders();
        setOrders(res.content || []);
        setError(null);
      } catch (err) {
        console.error('주문 목록 조회 실패', err);
        setError('주문 목록을 불러오는 데 실패했습니다.');
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [checking]);

  if (checking || loading) {
    return (
      <SellerLayout>
        <SellerHeader />
        <div className="p-6 max-w-4xl mx-auto">로딩 중...</div>
      </SellerLayout>
    );
  }

  return (
    <SellerLayout>
      <SellerHeader />
      <div className="p-6 max-w-4xl mx-auto">
        <h1 className="text-2xl font-bold mb-4">판매자 주문 목록</h1>

        {error ? (
          <p className="text-red-500">{error}</p>
        ) : orders.length === 0 ? (
          <p className="text-gray-400">등록된 주문이 없습니다.</p>
        ) : (
          <ul className="space-y-4">
            {orders.map((order) => (
              <li key={order.orderId} className="bg-white shadow-md rounded-lg p-4">
                <p>주문 ID: {order.orderId}</p>
                <p>주문일자: {new Date(order.orderedAt).toLocaleString()}</p>
                <p>고객명: {order.customerName}</p>
                <p>상품명: {order.productName}</p>
                <p>수량: {order.quantity}</p>

                <p className="flex justify-between items-center">
                  <span>배송 상태: {order.deliveryStatus}</span>
                  <button
                    onClick={() => router.push(`/seller/orders/${order.orderId}`)}
                    className="ml-4 bg-blue-600 text-white px-3 py-1 text-sm rounded hover:bg-blue-700"
                  >
                    배송 상세 정보 보기
                  </button>
                </p>
              </li>
            ))}
          </ul>
        )}
      </div>
    </SellerLayout>
  );
}
