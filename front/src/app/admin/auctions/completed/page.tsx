import React from 'react';

const CompletedAuctionListPage = () => {
  return (
    <div>
      <h2>완료된 경매 상품</h2>
      <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
        {[1,2,3].map((item) => (
          <div key={item} style={{ display: 'flex', background: '#eee', padding: 16 }}>
            <div style={{ width: 120, height: 120, background: '#ccc', marginRight: 24, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              상품 이미지
            </div>
            <div>
              <div>상품 이름</div>
              <div>경매 시작 시간</div>
              <div>경매 마감 시간</div>
              <div>낙찰자</div>
              <div>낙찰 금액</div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default CompletedAuctionListPage; 