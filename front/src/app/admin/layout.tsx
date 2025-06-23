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
      <AdminSidebar />
      <div style={{ flex: 1, background: '#f3f4f6' }}>
        <AdminHeader title={title} />
        <main style={{ padding: '24px' }}>{children}</main>
      </div>
    </div>
  );
} 