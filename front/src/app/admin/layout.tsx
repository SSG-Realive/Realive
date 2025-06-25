'use client';
import React from 'react';
import AdminSidebar from '../../components/AdminSidebar';
import AdminHeader from '../../components/AdminHeader';
import { usePathname } from 'next/navigation';

const pathTitleMap: { path: string; title: string }[] = [
  { path: '/admin/customers/list', title: '고객 관리' },
  { path: '/admin/customers/penalty', title: '사용자 패널티' },
  { path: '/admin/auction-management/list', title: '경매 목록' },
  { path: '/admin/auction-management/bid', title: '입찰 내역' },
  { path: '/admin/auction-management/register', title: '경매 등록' },
  { path: '/admin/review-management/list', title: '리뷰 목록' },
  { path: '/admin/review-management/reported', title: '리뷰 신고 관리' },
  { path: '/admin/review-management/qna', title: 'Q&A 관리' },
  { path: '/admin/customers', title: '회원' },
  { path: '/admin/auction-management', title: '경매' },
  { path: '/admin/review-management', title: '리뷰' },
  { path: '/admin/dashboard', title: '대시보드' },
  { path: '/admin/sellers', title: '판매자 관리' },
  { path: '/admin/products', title: '상품 관리' },
  { path: '/admin/settlement-management', title: '정산관리' },
];

export default function AdminLayout({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  // 로그인, 대문 페이지에서는 사이드바/상단바 숨김
  if (pathname === '/admin/login' || pathname === '/admin') {
    return <>{children}</>;
  }
  let found = pathTitleMap.find(item => pathname === item.path);
  if (!found) found = pathTitleMap.find(item => pathname.startsWith(item.path));
  const title = found ? found.title : '';
  return (
    <div style={{ display: 'flex' }}>
      {/* 데스크탑용 사이드바 */}
      <div className="hidden md:block">
        <div className="fixed left-0 top-0 h-screen w-[220px] z-40 bg-gray-900">
          <AdminSidebar />
        </div>
      </div>
      {/* 모바일 오버레이용 사이드바 */}
      <div id="mobile-sidebar-overlay" className="fixed inset-0 z-40 bg-black bg-opacity-40 hidden md:hidden" onClick={() => { document.getElementById('mobile-sidebar')?.classList.add('-translate-x-full'); document.getElementById('mobile-sidebar-overlay')?.classList.add('hidden'); }} />
      <div id="mobile-sidebar" className="fixed top-0 left-0 h-full w-full max-w-xs bg-gray-900 text-gray-200 z-50 p-6 transition-transform duration-300 -translate-x-full md:hidden">
        <button className="absolute top-4 right-4 text-white text-2xl" onClick={() => { document.getElementById('mobile-sidebar')?.classList.add('-translate-x-full'); document.getElementById('mobile-sidebar-overlay')?.classList.add('hidden'); }}>×</button>
      <AdminSidebar />
      </div>
      <div className="flex-1 w-full min-h-screen bg-gray-100 ml-[220px]">
        <AdminHeader title={title} />
        <main className="p-6">{children}</main>
      </div>
    </div>
  );
} 