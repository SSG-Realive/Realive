import Link from 'next/link';
import { useState, useRef, useEffect } from 'react';

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

  return (
    <aside style={{ width: 180, background: '#333', color: '#fff', minHeight: '100vh', padding: 24 }}>
      <h2 style={{ fontWeight: 'bold', fontSize: 22, marginBottom: 32 }}>관리자</h2>
      <nav>
        <ul style={{ listStyle: 'none', padding: 0, margin: 0, boxSizing: 'border-box', color: '#fff' }}>
          <li style={{ margin: '18px 0' }}><Link href="/admin/dashboard" style={{ color: '#fff' }}>대시보드</Link></li>
          <li style={{ margin: '18px 0', display: 'flex', alignItems: 'center' }}>
            <Link href="/admin/customers" style={{ flex: 1, color: '#fff' }}>회원</Link>
            <button
              aria-label="회원 하위 메뉴 열기"
              onClick={e => { e.stopPropagation(); setCustomerOpen(v => !v); }}
              style={{ background: 'none', border: 'none', color: '#fff', cursor: 'pointer', fontSize: 16, marginLeft: 4 }}
            >
              {customerOpen ? '▴' : '▾'}
            </button>
          </li>
          <Accordion open={customerOpen}>
            <ul style={{ listStyle: 'none', padding: 0, margin: 0, marginLeft: 16, boxSizing: 'border-box' }}>
              <li style={{ margin: 0, boxSizing: 'border-box', padding: '0 0 8px 0' }}><Link href="/admin/customers/list" style={{ color: '#fff' }}>고객 관리</Link></li>
              <li style={{ margin: 0, boxSizing: 'border-box', padding: '8px 0' }}><Link href="/admin/sellers" style={{ color: '#fff' }}>판매자 관리</Link></li>
              <li style={{ margin: 0, boxSizing: 'border-box', padding: '8px 0' }}><Link href="/admin/customers/penalty" style={{ color: '#fff' }}>사용자 패널티</Link></li>
            </ul>
          </Accordion>
          <li style={{ margin: '18px 0' }}><Link href="/admin/products" style={{ color: '#fff' }}>상품관리</Link></li>
          <li style={{ margin: '18px 0', display: 'flex', alignItems: 'center' }}>
            <Link href="/admin/auction-management" style={{ flex: 1, color: '#fff' }}>경매</Link>
            <button
              aria-label="경매 하위 메뉴 열기"
              onClick={e => { e.stopPropagation(); setAuctionOpen(v => !v); }}
              style={{ background: 'none', border: 'none', color: '#fff', cursor: 'pointer', fontSize: 16, marginLeft: 4 }}
            >
              {auctionOpen ? '▴' : '▾'}
            </button>
          </li>
          <Accordion open={auctionOpen}>
            <ul style={{ listStyle: 'none', padding: 0, margin: 0, marginLeft: 16, boxSizing: 'border-box' }}>
              <li style={{ margin: 0, boxSizing: 'border-box', padding: '0 0 8px 0' }}><Link href="/admin/auction-management/list" style={{ color: '#fff' }}>경매 목록</Link></li>
              <li style={{ margin: 0, boxSizing: 'border-box', padding: '8px 0' }}><Link href="/admin/auction-management/bid" style={{ color: '#fff' }}>입찰 내역</Link></li>
              <li style={{ margin: 0, boxSizing: 'border-box', padding: '8px 0' }}><Link href="/admin/auction-management/register" style={{ color: '#fff' }}>경매 등록</Link></li>
            </ul>
          </Accordion>
          <li style={{ margin: '18px 0' }}><Link href="/admin/settlement-management" style={{ color: '#fff' }}>정산관리</Link></li>
          <li style={{ margin: '18px 0', display: 'flex', alignItems: 'center' }}>
            <Link href="/admin/review-management" style={{ flex: 1, color: '#fff' }}>리뷰</Link>
            <button
              aria-label="리뷰 하위 메뉴 열기"
              onClick={e => { e.stopPropagation(); setReviewOpen(v => !v); }}
              style={{ background: 'none', border: 'none', color: '#fff', cursor: 'pointer', fontSize: 16, marginLeft: 4 }}
            >
              {reviewOpen ? '▴' : '▾'}
            </button>
          </li>
          <Accordion open={reviewOpen}>
            <ul style={{ listStyle: 'none', padding: 0, margin: 0, marginLeft: 16, boxSizing: 'border-box' }}>
              <li style={{ margin: 0, boxSizing: 'border-box', padding: '0 0 8px 0' }}><Link href="/admin/review-management/list" style={{ color: '#fff' }}>리뷰 목록</Link></li>
              <li style={{ margin: 0, boxSizing: 'border-box', padding: '8px 0' }}><Link href="/admin/review-management/reported" style={{ color: '#fff' }}>리뷰 신고 관리</Link></li>
              <li style={{ margin: 0, boxSizing: 'border-box', padding: '8px 0' }}><Link href="/admin/review-management/qna" style={{ color: '#fff' }}>Q&A 관리</Link></li>
            </ul>
          </Accordion>
        </ul>
      </nav>
    </aside>
  );
} 