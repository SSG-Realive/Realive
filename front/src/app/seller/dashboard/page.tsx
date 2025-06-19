// src/app/seller/dashboard/page.tsx 

'use client';

import SellerHeader from '@/components/seller/SellerHeader';
import SellerLayout from '@/components/layouts/SellerLayout';
import { getDashboard } from '@/service/seller/sellerService';
import { SellerDashboardResponse } from '@/types/seller/dashboard/sellerDashboardResponse';
import { useEffect, useState } from 'react';
import useSellerAuthGuard from '@/hooks/useSellerAuthGuard';

export default function SellerDashboardPage() {
  const checking = useSellerAuthGuard(); // ✅ 인증 확인 시작
  const [dashboard, setDashboard] = useState<SellerDashboardResponse | null>(null);

  useEffect(() => {
    // 💡 checking이 true이면(인증 확인 중이면) 아무것도 하지 않습니다.
    if (checking) {
      return;
    }

    const fetchDashboard = async () => {
      try {
        // ✅ checking이 false가 된 후에야 API를 호출하므로 안전합니다.
        const data = await getDashboard();
        setDashboard(data);
      } catch (err) {
        console.error('대시보드 정보 가져오기 실패', err);
      }
    };

    fetchDashboard();
  }, [checking]); // 💡 의존성 배열에 checking을 추가하여, 상태 변경 시 재실행되도록 합니다.

  // 인증 확인 중이거나, 데이터가 아직 없으면 로딩 UI를 표시합니다.
  // 로딩 중에도 헤더/레이아웃을 보여주어 깜빡임을 방지합니다.
  if (checking || !dashboard) {
    return (
      <>
        <SellerHeader />
        <SellerLayout>
          <main className="p-8 w-full">
            <h1 className="text-2xl font-bold mb-6">판매자 대시보드</h1>
            <div>로딩 중...</div>
          </main>
        </SellerLayout>
      </>
    );
  }

  return (
    <>
      <SellerHeader />
      <SellerLayout>
        <main className="p-8 w-full">
          <h1 className="text-2xl font-bold mb-6">판매자 대시보드</h1>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            <section className="bg-white p-6 rounded-lg shadow-sm">
              <h2 className="text-gray-600 text-sm font-semibold mb-2">등록 상품 수</h2>
              <p className="text-2xl font-bold text-gray-800">{dashboard.totalProductCount}개</p>
            </section>
            <section className="bg-white p-6 rounded-lg shadow-sm">
              <h2 className="text-gray-600 text-sm font-semibold mb-2">미답변 문의 수</h2>
              <p className="text-2xl font-bold text-red-500">{dashboard.unansweredQnaCount}건</p>
            </section>
            <section className="bg-white p-6 rounded-lg shadow-sm">
              <h2 className="text-gray-600 text-sm font-semibold mb-2">오늘 등록된 상품</h2>
              <p className="text-2xl font-bold text-gray-800">{dashboard.todayProductCount}개</p>
            </section>
            <section className="bg-white p-6 rounded-lg shadow-sm">
              <h2 className="text-gray-600 text-sm font-semibold mb-2">진행 중인 주문</h2>
              <p className="text-2xl font-bold text-blue-500">{dashboard.inProgressOrderCount}건</p>
            </section>
          </div>
        </main>
      </SellerLayout>
    </>
  );
}