'use client';

import { useEffect } from 'react';
import { useAuctionStore } from '@/store/admin/auctionStore';
import Link from 'next/link';

export default function AuctionListPage() {
  const { auctions, loading, fetchAuctions } = useAuctionStore();

  useEffect(() => {
    fetchAuctions();
  }, [fetchAuctions]);

  if (loading) return <div>로딩 중...</div>;

  return (
      <table>
        <thead>
        <tr>
          <th>ID</th>
          <th>경매명</th>
          <th>상품명</th>
          <th>시작가</th>
          <th>현재가</th>
          <th>상태</th>
          <th>시작일</th>
          <th>마감일</th>
          <th>판매자</th>
        </tr>
        </thead>
        <tbody>
        {auctions.map(a => (
            <tr key={a.id}>
              <td>
                <Link href={`/admin/auctions/${a.id}`}>{a.id}</Link>
              </td>
              <td>{a.name}</td>
              <td>{a.productName}</td>
              <td>{a.startPrice.toLocaleString()}원</td>
              <td>{a.currentPrice ? `${a.currentPrice.toLocaleString()}원` : '-'}</td>
              <td>
                {a.status === 'ACTIVE' ? '진행중' :
                    a.status === 'ENDED' ? '종료' :
                        a.status === 'CANCELLED' ? '취소됨' : a.status}
              </td>
              <td>{a.startTime.split('T')[0]}</td>
              <td>{a.endTime.split('T')[0]}</td>
              <td>{a.sellerName}</td>
            </tr>
        ))}
        </tbody>
      </table>
  );
}