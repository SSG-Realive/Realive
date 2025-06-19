'use client';

import Link from 'next/link';

interface AuctionCardProps {
  auction: any;
  isLast: boolean;
  refCallback: (node: HTMLLIElement | null) => void;
}

export default function AuctionCard({ auction, isLast, refCallback }: AuctionCardProps) {
  const { id, startPrice, currentPrice, endTime, status, adminProduct } = auction;
  const productName = adminProduct?.productName ?? '상품 없음';
  const imageUrl = adminProduct?.imageUrl ?? '/default-image.png';

  return (
    <li
      ref={isLast ? refCallback : null}
      style={{
        border: '1px solid #ccc',
        borderRadius: 6,
        padding: 10,
        backgroundColor: '#fff',
        boxShadow: '0 1px 3px rgb(0 0 0 / 0.1)',
        display: 'flex',
        flexDirection: 'column',
        height: '100%',
      }}
    >
      <Link href={`/auctions/${id}`} style={{ textDecoration: 'none', color: 'inherit', flexGrow: 1 }}>
        <div style={{ height: 140, marginBottom: 8, backgroundColor: '#eee', borderRadius: 4, overflow: 'hidden' }}>
          <img
            src={imageUrl}
            alt={productName}
            style={{ maxWidth: '100%', maxHeight: '100%', objectFit: 'contain' }}
            onError={(e) => ((e.target as HTMLImageElement).src = '/images/placeholder.png')}
          />
        </div>
        <div>
          <strong>{productName}</strong>
          <p>시작가: {startPrice.toLocaleString()}원</p>
          <p>현재가: {currentPrice.toLocaleString()}원</p>
          <p>종료일: {new Date(endTime).toLocaleString()}</p>
          <p>상태: {status}</p>
        </div>
      </Link>
    </li>
  );
}
