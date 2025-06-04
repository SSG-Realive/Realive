'use client';

import Header from '@/components/Header';
import SellerLayout from '@/components/layouts/SellerLayout';
import useSellerAuthGuard from '@/hooks/useSellerAuthGuard';
import { getDashboard, SellerDashboardResponse } from '@/service/sellerService';
import { useEffect, useState } from 'react';

export default function SellerDashboardPage() {
    const checking = useSellerAuthGuard();

    const [dashboard, setDashboard] = useState<SellerDashboardResponse | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(()=> {
        if (checking) return;

        const fetchDashboard = async () => {
            try {
                const data = await getDashboard();
                setDashboard(data);
            } catch(err) {
                console.error('대시보드 정보 가져오기 실패', err);

            }finally {
                setLoading(false);
            }            
        };
        fetchDashboard();
    }, [checking]);

    if (loading) return <div className="p-8">로딩 중...</div>;
    if (!dashboard) return <div className="p-8">데이터 없음</div>;
    if (checking) return <div className="p-8">인증 확인 중...</div>;
  return (
    <>
      <Header />
      <SellerLayout>
        <main className="p-8 w-full">
          <h1 className="text-2xl font-bold mb-6">판매자 대시보드</h1>

          <div className="grid grid-cols-2 gap-6">
            <section className="bg-white p-6 rounded shadow">
              <h2 className="text-lg font-semibold mb-2">등록 상품 수</h2>
              <p>{dashboard.totalProducts}개</p>
            </section>

            <section className="bg-white p-6 rounded shadow">
              <h2 className="text-lg font-semibold mb-2">미답변 문의 수</h2>
              <p>{dashboard.unansweredQna}건</p>
            </section>

            <section className="bg-white p-6 rounded shadow">
              <h2 className="text-lg font-semibold mb-2">오늘 등록된 상품</h2>
              <p>{dashboard.todayProducts}개</p>
            </section>

            <section className="bg-white p-6 rounded shadow">
              <h2 className="text-lg font-semibold mb-2">진행 중인 주문</h2>
              <p>{dashboard.activeOrders}건</p>
            </section>
          </div>
        </main>
      </SellerLayout>
    </>
  );
}
