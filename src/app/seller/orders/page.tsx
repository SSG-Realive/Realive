'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { getSellerOrders } from '@/service/seller/sellerOrderService';
import { SellerOrderResponse } from '@/types/seller/sellerorder/sellerOrder';
import SellerHeader from '@/components/seller/SellerHeader';
import SellerLayout from '@/components/layouts/SellerLayout';
import useSellerAuthGuard from '@/hooks/useSellerAuthGuard';
import { PageResponseForOrder } from '@/types/seller/page/pageResponseForOrder';
import { Armchair, Truck, CheckCircle, Clock, Eye, Search } from 'lucide-react';

export default function SellerOrderListPage() {
  const checking = useSellerAuthGuard(); // ✅ 인증 확인
  const [orders, setOrders] = useState<SellerOrderResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [searchKeyword, setSearchKeyword] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
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

  // 통계 계산
  const totalOrders = orders.length;
  const preparingOrders = orders.filter(order => order.deliveryStatus === 'DELIVERY_PREPARING').length;
  const inProgressOrders = orders.filter(order => order.deliveryStatus === 'DELIVERY_IN_PROGRESS').length;
  const completedOrders = orders.filter(order => order.deliveryStatus === 'DELIVERY_COMPLETED').length;

  // 필터링된 주문 목록
  const filteredOrders = orders.filter(order => {
    const matchesKeyword = searchKeyword === '' || 
      order.customerName.toLowerCase().includes(searchKeyword.toLowerCase()) ||
      order.productName.toLowerCase().includes(searchKeyword.toLowerCase()) ||
      order.orderId.toString().includes(searchKeyword);
    
    const matchesStatus = statusFilter === '' || order.deliveryStatus === statusFilter;
    
    return matchesKeyword && matchesStatus;
  });

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'INIT':
        return <span className="px-2 py-1 rounded text-xs font-bold bg-[#e9dec7] text-[#bfa06a]">주문 접수</span>;
      case 'DELIVERY_PREPARING':
        return <span className="px-2 py-1 rounded text-xs font-bold bg-[#fbeee0] text-[#bfa06a]">배송 준비</span>;
      case 'DELIVERY_IN_PROGRESS':
        return <span className="px-2 py-1 rounded text-xs font-bold bg-[#bfa06a] text-[#5b4636]">배송 중</span>;
      case 'DELIVERY_COMPLETED':
        return <span className="px-2 py-1 rounded text-xs font-bold bg-green-100 text-green-700">배송 완료</span>;
      default:
        return <span className="px-2 py-1 rounded text-xs font-bold bg-[#e9dec7] text-[#bfa06a]">{status}</span>;
    }
  };

  if (checking || loading) {
    return (
      <div className="w-full max-w-full min-h-screen overflow-x-hidden bg-[#a89f91] flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-[#bfa06a] mx-auto mb-4"></div>
          <p className="text-[#5b4636]">주문 정보를 불러오는 중...</p>
        </div>
      </div>
    );
  }

  return (
    <SellerLayout>
      <div className="flex-1 w-full h-full px-4 py-8 bg-[#a89f91]">
        <h1 className="text-xl md:text-2xl font-bold mb-6 text-[#5b4636]">주문 관리</h1>

        {/* 상단 통계 카드 */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 md:gap-6 mb-6">
          <section className="bg-[#e9dec7] p-4 md:p-6 rounded-lg shadow-sm border border-[#bfa06a] flex items-center justify-between">
            <div>
              <h2 className="text-[#5b4636] text-sm font-semibold mb-2">총 주문 수</h2>
              <p className="text-xl md:text-2xl font-bold text-[#5b4636]">{totalOrders}건</p>
            </div>
            <Armchair className="w-8 h-8 text-[#bfa06a]" />
          </section>
          <section className="bg-[#e9dec7] p-4 md:p-6 rounded-lg shadow-sm border border-[#bfa06a] flex items-center justify-between">
            <div>
              <h2 className="text-[#5b4636] text-sm font-semibold mb-2">배송 준비</h2>
              <p className="text-xl md:text-2xl font-bold text-[#bfa06a]">{preparingOrders}건</p>
            </div>
            <Clock className="w-8 h-8 text-[#bfa06a]" />
          </section>
          <section className="bg-[#e9dec7] p-4 md:p-6 rounded-lg shadow-sm border border-[#bfa06a] flex items-center justify-between">
            <div>
              <h2 className="text-[#5b4636] text-sm font-semibold mb-2">배송 중</h2>
              <p className="text-xl md:text-2xl font-bold text-[#5b4636]">{inProgressOrders}건</p>
            </div>
            <Truck className="w-8 h-8 text-[#bfa06a]" />
          </section>
          <section className="bg-[#e9dec7] p-4 md:p-6 rounded-lg shadow-sm border border-[#bfa06a] flex items-center justify-between">
            <div>
              <h2 className="text-[#5b4636] text-sm font-semibold mb-2">배송 완료</h2>
              <p className="text-xl md:text-2xl font-bold text-[#388e3c]">{completedOrders}건</p>
            </div>
            <CheckCircle className="w-8 h-8 text-[#bfa06a]" />
          </section>
        </div>

        {/* 검색/필터 영역 */}
        <div className="flex flex-col md:flex-row gap-3 md:gap-4 mb-6 items-center">
          <input
            type="text"
            placeholder="고객명, 상품명, 주문번호 검색"
            value={searchKeyword}
            onChange={(e) => setSearchKeyword(e.target.value)}
            className="flex-1 border border-[#bfa06a] rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-[#bfa06a] bg-[#e9dec7] text-[#5b4636]"
          />
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="border border-[#bfa06a] rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-[#bfa06a] bg-[#e9dec7] text-[#5b4636]"
          >
            <option value="">전체 상태</option>
            <option value="INIT">주문 접수</option>
            <option value="DELIVERY_PREPARING">배송 준비</option>
            <option value="DELIVERY_IN_PROGRESS">배송 중</option>
            <option value="DELIVERY_COMPLETED">배송 완료</option>
          </select>
          <button 
            className="bg-[#bfa06a] text-[#4b3a2f] px-4 py-2 rounded-md hover:bg-[#5b4636] hover:text-[#e9dec7] flex items-center gap-2"
          >
            <Search className="w-4 h-4" />
            검색
          </button>
        </div>

        {/* 주문 리스트 */}
        {error ? (
          <div className="bg-[#fbeee0] border border-[#bfa06a] rounded-lg p-4">
            <p className="text-[#b94a48]">{error}</p>
          </div>
        ) : filteredOrders.length === 0 ? (
          <div className="bg-[#e9dec7] border border-[#bfa06a] rounded-lg p-8 text-center">
            <Armchair className="w-12 h-12 text-[#bfa06a] mx-auto mb-4" />
            <p className="text-[#bfa06a] text-lg">주문이 없습니다.</p>
          </div>
        ) : (
          <div className="overflow-x-auto bg-[#e9dec7] rounded-lg shadow-sm border border-[#bfa06a]">
            <table className="min-w-full divide-y divide-[#bfa06a]">
              <thead className="bg-[#e9dec7]">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-[#bfa06a] uppercase tracking-wider">주문번호</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-[#bfa06a] uppercase tracking-wider">주문일시</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-[#bfa06a] uppercase tracking-wider">고객명</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-[#bfa06a] uppercase tracking-wider">상품명</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-[#bfa06a] uppercase tracking-wider">수량</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-[#bfa06a] uppercase tracking-wider">배송상태</th>
                  <th className="px-6 py-3 text-center text-xs font-medium text-[#bfa06a] uppercase tracking-wider">액션</th>
                </tr>
              </thead>
              <tbody className="bg-[#e9dec7] divide-y divide-[#bfa06a]">
                {filteredOrders.map((order) => (
                  <tr key={order.orderId} className="hover:bg-[#bfa06a] transition-colors">
                    <td className="px-6 py-4 whitespace-nowrap font-semibold text-[#5b4636]">#{order.orderId}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-[#5b4636]">
                      {new Date(order.orderedAt).toLocaleDateString()}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap font-medium text-[#5b4636]">{order.customerName}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-[#5b4636]">{order.productName}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-[#5b4636]">{order.quantity}개</td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      {getStatusBadge(order.deliveryStatus)}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-center">
                      <button
                        onClick={() => router.push(`/seller/orders/${order.orderId}`)}
                        className="inline-flex items-center gap-1 bg-[#bfa06a] text-[#4b3a2f] px-3 py-1.5 rounded hover:bg-[#5b4636] hover:text-[#e9dec7] text-sm"
                      >
                        <Eye className="w-4 h-4" /> 상세 보기
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </SellerLayout>
  );
}
