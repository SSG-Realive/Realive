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
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const router = useRouter();

  const toggleSidebar = () => {
    setSidebarOpen(!sidebarOpen);
  };

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
          <div className="w-full max-w-full min-h-screen overflow-x-hidden bg-gray-50 px-4 md:px-8 py-6">
            <div className="flex items-center justify-center py-8">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
              <span className="ml-3 text-gray-600">로딩 중...</span>
            </div>
          </div>
      </SellerLayout>
    );
  }

  return (
    <SellerLayout>
        <div className="w-full max-w-full min-h-screen overflow-x-hidden bg-gray-50 px-4 md:px-8 py-6">
          <h1 className="text-xl md:text-2xl font-bold mb-4 md:mb-6">판매자 주문 목록</h1>

        {error ? (
            <div className="bg-red-50 border border-red-200 rounded-lg p-4">
              <p className="text-red-600">{error}</p>
            </div>
        ) : orders.length === 0 ? (
            <div className="bg-white border border-gray-200 rounded-lg p-8 text-center">
              <p className="text-gray-500 text-lg">등록된 주문이 없습니다.</p>
            </div>
        ) : (
            <div className="grid gap-4">
            {orders.map((order) => (
                <div key={order.orderId} className="bg-white border border-gray-200 rounded-lg p-4 shadow-sm hover:shadow-md transition-shadow">
                  <div className="space-y-3">
                    <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-2">
                      <h3 className="font-semibold text-lg text-gray-800">주문 ID: {order.orderId}</h3>
                      <span className="text-sm text-gray-500">
                        {new Date(order.orderedAt).toLocaleString()}
                      </span>
                    </div>
                    
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-3 text-sm">
                      <div>
                        <span className="text-gray-600">고객명:</span>
                        <span className="ml-2 font-medium">{order.customerName}</span>
                      </div>
                      <div>
                        <span className="text-gray-600">상품명:</span>
                        <span className="ml-2 font-medium">{order.productName}</span>
                      </div>
                      <div>
                        <span className="text-gray-600">수량:</span>
                        <span className="ml-2 font-medium">{order.quantity}개</span>
                      </div>
                      <div>
                        <span className="text-gray-600">배송 상태:</span>
                        <span className="ml-2 font-medium text-blue-600">{order.deliveryStatus}</span>
                      </div>
                    </div>
                    
                    <div className="pt-2 border-t border-gray-100">
                  <button
                    onClick={() => router.push(`/seller/orders/${order.orderId}`)}
                        className="w-full md:w-auto bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 transition-colors"
                  >
                    배송 상세 정보 보기
                  </button>
                    </div>
                  </div>
                </div>
            ))}
            </div>
        )}
      </div>
    </SellerLayout>
  );
}
