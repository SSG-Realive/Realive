'use client';
import Link from 'next/link';
import { useState, useRef, useEffect } from 'react';
import { usePathname } from 'next/navigation';

function Accordion({ open, children }: { open: boolean; children: React.ReactNode }) {
  const ref = useRef<HTMLDivElement>(null);
  const [maxHeight, setMaxHeight] = useState(0);
  const [shouldRender, setShouldRender] = useState(open);

  useEffect(() => {
    if (open) {
      setShouldRender(true);
      requestAnimationFrame(() => {
        if (ref.current) setMaxHeight(ref.current.scrollHeight);
      });
    } else {
      if (ref.current) setMaxHeight(0);
      const timeout = setTimeout(() => setShouldRender(false), 300);
      return () => clearTimeout(timeout);
    }
  }, [open]);

  if (!shouldRender && !open) return null;

  return (
    <div
      style={{
        maxHeight,
        overflow: 'hidden',
        transition: 'max-height 0.3s cubic-bezier(0.4,0,0.2,1)',
        boxSizing: 'border-box',
      }}
    >
      <div ref={ref} style={{ boxSizing: 'border-box' }}>{children}</div>
    </div>
  );
}

export default function AdminSidebar() {
  const [auctionOpen, setAuctionOpen] = useState(false);
  const [reviewOpen, setReviewOpen] = useState(false);
  const [customerOpen, setCustomerOpen] = useState(false);
  const [productOpen, setProductOpen] = useState(false);
  const pathname = usePathname();

  const getLinkStyle = (path: string) => {
    const isActive = pathname === path;
    return {
      padding: '8px 16px',
      borderRadius: '4px',
      color: isActive ? '#ffffff' : '#e5e7eb',
      backgroundColor: isActive ? '#14b8a6' : 'transparent',
      fontWeight: isActive ? 'bold' : 'normal',
      textDecoration: 'none',
    };
  };

  const getSubLinkStyle = (path: string) => {
    const isActive = pathname.startsWith(path);
    return {
      display: 'block',
      padding: '6px 12px',
      borderRadius: '4px',
      color: isActive ? '#ffffff' : '#d1d5db',
      backgroundColor: isActive ? '#14b8a6' : 'transparent',
      textDecoration: 'none',
      fontSize: '14px',
    };
  };

  return (
    <aside
      className="w-full md:w-[220px] bg-gray-900 text-gray-200 min-h-screen h-full p-6"
    >
      <h2 className="font-bold text-2xl mb-8 text-teal-500 px-4">관리자</h2>
      <nav>
        <ul className="list-none p-0 m-0 box-border">
          <li className="my-2">
            <Link href="/admin/dashboard" className="block px-4 py-3 rounded text-lg font-semibold hover:bg-teal-600 transition-colors" style={getLinkStyle('/admin/dashboard')}>
              대시보드
            </Link>
          </li>
          <li className="my-2">
            <div className="flex items-center px-4 py-3 rounded hover:bg-teal-600 transition-colors text-lg font-semibold" style={getLinkStyle('/admin/member-management')}>
              <Link href="/admin/member-management" className="flex-1 no-underline text-inherit">
                회원
              </Link>
              <button
                aria-label="회원 하위 메뉴 열기"
                onClick={(e) => { e.stopPropagation(); e.preventDefault(); setCustomerOpen(v => !v); }}
                className="bg-none border-none text-inherit cursor-pointer text-base"
              >
                {customerOpen ? '▴' : '▾'}
              </button>
            </div>
            <Accordion open={customerOpen}>
              <ul className="list-none p-0 m-0 ml-7 border-l border-gray-600">
                <li className="w-full"><Link href="/admin/member-management/customer" className="block w-full text-lg px-6 py-4 pl-2 rounded hover:bg-teal-600 transition-colors md:text-base md:px-3 md:py-2 md:pl-6" style={getSubLinkStyle('/admin/member-management/customer')}>고객 관리</Link></li>
                <li className="w-full"><Link href="/admin/member-management/seller" className="block w-full text-lg px-6 py-4 pl-2 rounded hover:bg-teal-600 transition-colors md:text-base md:px-3 md:py-2 md:pl-6" style={getSubLinkStyle('/admin/member-management/seller')}>판매자 관리</Link></li>
                <li className="w-full"><Link href="/admin/member-management/penalty" className="block w-full text-lg px-6 py-4 pl-2 rounded hover:bg-teal-600 transition-colors md:text-base md:px-3 md:py-2 md:pl-6" style={getSubLinkStyle('/admin/member-management/penalty')}>사용자 패널티</Link></li>
              </ul>
            </Accordion>
          </li>
          <li className="my-2">
            <div className="flex items-center px-4 py-3 rounded hover:bg-teal-600 transition-colors text-lg font-semibold" style={getLinkStyle('/admin/product-managemnet')}>
              <Link href="/admin/product-management" className="flex-1 no-underline text-inherit">
              상품
            </Link>
            <button
                aria-label="상품 하위 메뉴 열기"
                onClick={(e) => { e.stopPropagation(); e.preventDefault(); setProductOpen(v => !v); }}
                style={{ background: 'none', border: 'none', color: 'inherit', cursor: 'pointer', fontSize: 16 }}
            >
              {productOpen ? '▴' : '▾'}
            </button>
            </div>
          </li>
          <Accordion open={productOpen}>
            <ul style={{ listStyle: 'none', padding: 0, margin: 0, marginLeft: 28, borderLeft: '1px solid #4b5563' }}>
               <li className="w-full"><Link href="/admin/product-management/list" className="block w-full text-lg px-6 py-4 pl-2 rounded hover:bg-teal-600 transition-colors md:text-base md:px-3 md:py-2 md:pl-6" style={getSubLinkStyle('/admin/products/list')}>전체 상품 조회</Link></li>
               <li className="w-full"><Link href="/admin/product-management/owned-products" className="block w-full text-lg px-6 py-4 pl-2 rounded hover:bg-teal-600 transition-colors md:text-base md:px-3 md:py-2 md:pl-6" style={getSubLinkStyle('/admin/owned-products')}>관리자 상품 조회</Link></li>
            </ul>
          </Accordion>
          <li className="my-2">
            <div className="flex items-center px-4 py-3 rounded hover:bg-teal-600 transition-colors text-lg font-semibold" style={getLinkStyle('/admin/auction-management')}>
              <Link href="/admin/auction-management" className="flex-1 no-underline text-inherit">
                경매
              </Link>
              <button
                aria-label="경매 하위 메뉴 열기"
                onClick={(e) => { e.stopPropagation(); e.preventDefault(); setAuctionOpen(v => !v); }}
                className="bg-none border-none text-inherit cursor-pointer text-base"
              >
                {auctionOpen ? '▴' : '▾'}
              </button>
            </div>
            <Accordion open={auctionOpen}>
              <ul className="list-none p-0 m-0 ml-7 border-l border-gray-600">
                <li className="py-2 pl-6 sm:pl-4 w-full"><Link href="/admin/auction-management/list" className="block text-base px-3 py-2 sm:text-lg sm:py-3 sm:px-4 rounded hover:bg-teal-600 transition-colors w-full" style={getSubLinkStyle('/admin/auction-management/list')}>경매 목록</Link></li>
                <li className="py-2 pl-6 sm:pl-4 w-full"><Link href="/admin/auction-management/bid" className="block text-base px-3 py-2 sm:text-lg sm:py-3 sm:px-4 rounded hover:bg-teal-600 transition-colors w-full" style={getSubLinkStyle('/admin/auction-management/bid')}>입찰 내역</Link></li>
              </ul>
            </Accordion>
          </li>
          <li className="my-2">
            <Link href="/admin/settlement-management" className="block px-4 py-3 rounded text-lg font-semibold hover:bg-teal-600 transition-colors" style={getLinkStyle('/admin/settlement-management')}>
              정산
            </Link>
          </li>
          <li className="my-2">
            <div className="flex items-center px-4 py-3 rounded hover:bg-teal-600 transition-colors text-lg font-semibold cursor-pointer" style={getLinkStyle('/admin/review-management')}
              onClick={() => setReviewOpen(v => !v)}
            >
              <span className="flex-1 no-underline text-inherit select-none">
                고객 피드백
              </span>
              <button
                aria-label="리뷰 하위 메뉴 열기"
                onClick={(e) => { e.stopPropagation(); e.preventDefault(); setReviewOpen(v => !v); }}
                className="bg-none border-none text-inherit cursor-pointer text-base"
              >
                {reviewOpen ? '▴' : '▾'}
              </button>
            </div>
            <Accordion open={reviewOpen}>
              <ul className="list-none p-0 m-0 ml-7 border-l border-gray-600">
                <li className="w-full"><Link href="/admin/review-management/list" className="block w-full text-lg px-6 py-4 pl-2 rounded hover:bg-teal-600 transition-colors md:text-base md:px-3 md:py-2 md:pl-6" style={getSubLinkStyle('/admin/review-management/list')}>리뷰 목록</Link></li>
                <li className="w-full"><Link href="/admin/review-management/seller-qna" className="block w-full text-lg px-6 py-4 pl-2 rounded hover:bg-teal-600 transition-colors md:text-base md:px-3 md:py-2 md:pl-6" style={getSubLinkStyle('/admin/review-management/seller-qna')}>Q&A 관리</Link></li>
              </ul>
            </Accordion>
          </li>
        </ul>
      </nav>
    </aside>
  );
} 