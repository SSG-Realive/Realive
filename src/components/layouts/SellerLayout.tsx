// components/layouts/SellerLayout.tsx
'use client';

import { ReactNode, useState } from "react";
import SellerSidebar from "../seller/SellerSidebar";
import SellerHeader from "../seller/SellerHeader";

interface SellerLayoutProps {
  children: ReactNode;
}

export default function SellerLayout({ children }: SellerLayoutProps) {
  const [sidebarOpen, setSidebarOpen] = useState(false);

  const toggleSidebar = () => setSidebarOpen((prev) => !prev);

  return (
    <div className="min-h-screen h-screen w-full bg-gray-100 flex flex-col">
      {/* 헤더: 항상 상단 고정 (fixed 제거!) */}
      <div>
        <SellerHeader toggleSidebar={toggleSidebar} />
      </div>

      {/* 오버레이: 모바일에서만, 사이드바 열렸을 때만 */}
      <div
        className={`fixed inset-0 bg-black transition-opacity duration-300 z-40 lg:hidden ${
          sidebarOpen ? 'opacity-50 pointer-events-auto' : 'opacity-0 pointer-events-none'
        }`}
        onClick={() => setSidebarOpen(false)}
      />

      {/* 레이아웃: 데스크탑 flex-row, 모바일 block */}
      <div className="flex flex-col lg:flex-row flex-1 w-full min-h-screen">
        {/* 모바일 사이드바: 항상 렌더링하되 transform으로 애니메이션 */}
        <div className="lg:hidden">
          <SellerSidebar 
            onClose={() => setSidebarOpen(false)} 
            className={`fixed z-50 top-0 left-0 min-h-screen h-full w-60 flex-shrink-0 flex flex-col transition-transform duration-300 ease-in-out ${
              sidebarOpen ? 'translate-x-0' : '-translate-x-full'
            }`} 
          />
        </div>
        {/* 데스크탑 사이드바: 항상 화면에 고정 */}
        <div className="hidden lg:block">
          <SellerSidebar className="fixed top-0 left-0 h-screen w-60 flex flex-col z-30" />
        </div>
        {/* 본문: flex-1로 꽉 차게, 데스크탑에서 사이드바만큼 마진 */}
        <main className="flex-1 w-full min-h-screen px-4 py-8 bg-[#a89f91] lg:ml-60">
          {children}
        </main>
      </div>
    </div>
  );
}
