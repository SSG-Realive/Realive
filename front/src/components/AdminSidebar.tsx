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
    <aside style={{ width: 220, background: '#111827', color: '#e5e7eb', minHeight: '100vh', padding: '24px 12px' }}>
      <h2 style={{ fontWeight: 'bold', fontSize: 24, marginBottom: 32, color: '#14b8a6', padding: '0 16px' }}>
        관리자
      </h2>
      <nav>
        <ul style={{ listStyle: 'none', padding: 0, margin: 0, boxSizing: 'border-box' }}>
          <li style={{ margin: '8px 0' }}>
            <Link href="/admin/dashboard" style={{ ...getLinkStyle('/admin/dashboard'), display: 'block' }}>
              대시보드
            </Link>
          </li>
          <li style={{ margin: '8px 0' }}>
            <div style={{ display: 'flex', alignItems: 'center', ...getLinkStyle('/admin/customers') }}>
              <Link href="/admin/customers" style={{ flex: 1, textDecoration: 'none', color: 'inherit' }}>
                회원
              </Link>
              <button
                aria-label="회원 하위 메뉴 열기"
                onClick={(e) => { e.stopPropagation(); e.preventDefault(); setCustomerOpen(v => !v); }}
                style={{ background: 'none', border: 'none', color: 'inherit', cursor: 'pointer', fontSize: 16 }}
              >
                {customerOpen ? '▴' : '▾'}
              </button>
            </div>
          </li>
          <Accordion open={customerOpen}>
            <ul style={{ listStyle: 'none', padding: 0, margin: 0, marginLeft: 28, borderLeft: '1px solid #4b5563' }}>
              <li style={{ padding: '4px 0 4px 12px' }}><Link href="/admin/customers/list" style={getSubLinkStyle('/admin/customers/list')}>고객 관리</Link></li>
              <li style={{ padding: '4px 0 4px 12px' }}><Link href="/admin/sellers" style={getSubLinkStyle('/admin/sellers')}>판매자 관리</Link></li>
              <li style={{ padding: '4px 0 4px 12px' }}><Link href="/admin/customers/penalty" style={getSubLinkStyle('/admin/customers/penalty')}>사용자 패널티</Link></li>
            </ul>
          </Accordion>
          <li style={{ margin: '8px 0' }}>
            <div style={{ display: 'flex', alignItems: 'center', ...getLinkStyle('/admin/products') }}>
            <Link href="/admin/products" style={{ flex: 1, textDecoration: 'none', color: 'inherit' }}>
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
              <li style={{ padding: '4px 0 4px 12px' }}><Link href="/admin/products/list" style={getSubLinkStyle('/admin/products/list')}>전체 상품 조회</Link></li>
              <li style={{ padding: '4px 0 4px 12px' }}><Link href="/admin/owned-products" style={getSubLinkStyle('/admin/owned-products')}>관리자 상품 조회</Link></li>
            </ul>
          </Accordion>
          <li style={{ margin: '8px 0' }}>
            <div style={{ display: 'flex', alignItems: 'center', ...getLinkStyle('/admin/auction-management') }}>
              <Link href="/admin/auction-management" style={{ flex: 1, textDecoration: 'none', color: 'inherit' }}>
                경매
              </Link>
              <button
                aria-label="경매 하위 메뉴 열기"
                onClick={(e) => { e.stopPropagation(); e.preventDefault(); setAuctionOpen(v => !v); }}
                style={{ background: 'none', border: 'none', color: 'inherit', cursor: 'pointer', fontSize: 16 }}
              >
                {auctionOpen ? '▴' : '▾'}
              </button>
            </div>
          </li>
          <Accordion open={auctionOpen}>
            <ul style={{ listStyle: 'none', padding: 0, margin: 0, marginLeft: 28, borderLeft: '1px solid #4b5563' }}>
              <li style={{ padding: '4px 0 4px 12px' }}><Link href="/admin/auction-management/list" style={getSubLinkStyle('/admin/auction-management/list')}>경매 목록</Link></li>
              <li style={{ padding: '4px 0 4px 12px' }}><Link href="/admin/auction-management/bid" style={getSubLinkStyle('/admin/auction-management/bid')}>입찰 내역</Link></li>
              <li style={{ padding: '4px 0 4px 12px' }}><Link href="/admin/auction-management/register" style={getSubLinkStyle('/admin/auction-management/register')}>경매 등록</Link></li>
            </ul>
          </Accordion>
          <li style={{ margin: '8px 0' }}>
            <Link href="/admin/settlement-management" style={{ ...getLinkStyle('/admin/settlement-management'), display: 'block' }}>
              정산관리
            </Link>
          </li>
          <li style={{ margin: '8px 0' }}>
            <div style={{ display: 'flex', alignItems: 'center', ...getLinkStyle('/admin/review-management') }}>
              <Link href="/admin/review-management" style={{ flex: 1, textDecoration: 'none', color: 'inherit' }}>
                리뷰
              </Link>
              <button
                aria-label="리뷰 하위 메뉴 열기"
                onClick={(e) => { e.stopPropagation(); e.preventDefault(); setReviewOpen(v => !v); }}
                style={{ background: 'none', border: 'none', color: 'inherit', cursor: 'pointer', fontSize: 16 }}
              >
                {reviewOpen ? '▴' : '▾'}
              </button>
            </div>
          </li>
          <Accordion open={reviewOpen}>
            <ul style={{ listStyle: 'none', padding: 0, margin: 0, marginLeft: 28, borderLeft: '1px solid #4b5563' }}>
              <li style={{ padding: '4px 0 4px 12px' }}><Link href="/admin/review-management/list" style={getSubLinkStyle('/admin/review-management/list')}>리뷰 목록</Link></li>
              <li style={{ padding: '4px 0 4px 12px' }}><Link href="/admin/review-management/reported" style={getSubLinkStyle('/admin/review-management/reported')}>리뷰 신고 관리</Link></li>
              <li style={{ padding: '4px 0 4px 12px' }}><Link href="/admin/review-management/qna" style={getSubLinkStyle('/admin/review-management/qna')}>Q&A 관리</Link></li>
            </ul>
          </Accordion>
        </ul>
      </nav>
    </aside>
  );
} 