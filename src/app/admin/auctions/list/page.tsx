+'use client';
import React from 'react';
import { useRouter } from 'next/navigation';

const AuctionListPage = () => {
  const router = useRouter();
  return (
    <div>
      <h2>준비중인 경매 상품</h2>
      <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
        {[1,2,3].map((item) => (
          <div key={item} style={{ display: 'flex', background: '#eee', padding: 16 }}>
            <div style={{ width: 120, height: 120, background: '#ccc', marginRight: 24, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              상품 이미지
            </div>
            <div style={{ flex: 1 }}>
              <div>상품 이름</div>
              <div>경매 시작 시간</div>
              <div>경매 마감 시간</div>
              <div>즉시 구매가</div>
            </div>
            <button onClick={() => router.push(`/admin/auctions/${item}`)}>상세보기</button>
          </div>
        ))}
      </div>
    </div>
  );
};

export default AuctionListPage; 