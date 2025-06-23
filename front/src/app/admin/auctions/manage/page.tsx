import React from 'react';

const auctions = [
  { id: 1, name: '의자 경매', start: 50000, buyNow: 100000, status: '진행중', startDate: '2024-06-10', endDate: '2024-06-15', seller: 'Hong', action: 'View' },
  { id: 2, name: '책상 경매', start: 120000, buyNow: 200000, status: '종료', startDate: '2024-06-01', endDate: '2024-06-05', seller: 'Kim', action: 'View' },
];

export default function AuctionManagePage() {
  return (
    <div style={{ display: 'flex', minHeight: '100vh', background: '#f4f4f4' }}>
      {/* 사이드바 */}
      <aside style={{ width: 120, background: '#e0e0e0', padding: 16 }}>
        <nav>
          <ul style={{ listStyle: 'none', padding: 0, margin: 0 }}>
            <li style={{ margin: '16px 0' }}>고객</li>
            <li style={{ margin: '16px 0' }}>판매자</li>
            <li style={{ margin: '16px 0' }}>주문</li>
            <li style={{ margin: '16px 0' }}>상품</li>
            <li style={{ margin: '16px 0', fontWeight: 'bold' }}>경매</li>
            <li style={{ margin: '16px 0' }}>FAQ</li>
          </ul>
        </nav>
      </aside>
      {/* 메인 */}
      <main style={{ flex: 1, background: '#fff', padding: 32 }}>
        <header style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
          <h2 style={{ fontWeight: 'bold', fontSize: 24 }}>경매관리</h2>
          <span style={{ fontWeight: 'bold' }}>admin</span>
        </header>
        {/* 검색/필터 */}
        <div style={{ marginBottom: 16, display: 'flex', gap: 8 }}>
          <input placeholder="경매명, 판매자, 상태 검색" style={{ padding: 4, border: '1px solid #ccc', borderRadius: 4 }} />
          <select style={{ padding: 4, border: '1px solid #ccc', borderRadius: 4 }}>
            <option>상태 전체</option>
            <option>진행중</option>
            <option>종료</option>
          </select>
          <button style={{ padding: '4px 16px', background: '#222', color: '#fff', border: 'none', borderRadius: 4 }}>검색</button>
        </div>
        {/* 테이블 */}
        <table style={{ width: '100%', borderCollapse: 'collapse', background: '#fff', fontSize: 15 }}>
          <thead>
            <tr style={{ background: '#f7f7f7' }}>
              <th style={{ padding: 8, border: '1px solid #eee' }}>경매명</th>
              <th style={{ padding: 8, border: '1px solid #eee' }}>시작가</th>
              <th style={{ padding: 8, border: '1px solid #eee' }}>즉시구매가</th>
              <th style={{ padding: 8, border: '1px solid #eee' }}>상태</th>
              <th style={{ padding: 8, border: '1px solid #eee' }}>시작일</th>
              <th style={{ padding: 8, border: '1px solid #eee' }}>마감일</th>
              <th style={{ padding: 8, border: '1px solid #eee' }}>판매자</th>
              <th style={{ padding: 8, border: '1px solid #eee' }}>Action</th>
            </tr>
          </thead>
          <tbody>
            {auctions.map((a) => (
              <tr key={a.id}>
                <td style={{ padding: 8, border: '1px solid #eee' }}>{a.name}</td>
                <td style={{ padding: 8, border: '1px solid #eee' }}>{a.start.toLocaleString()}</td>
                <td style={{ padding: 8, border: '1px solid #eee' }}>{a.buyNow.toLocaleString()}</td>
                <td style={{ padding: 8, border: '1px solid #eee' }}>
                  <span style={{ background: a.status === '진행중' ? '#4caf50' : '#bdbdbd', color: '#fff', padding: '2px 10px', borderRadius: 4, fontWeight: 'bold' }}>{a.status}</span>
                </td>
                <td style={{ padding: 8, border: '1px solid #eee' }}>{a.startDate}</td>
                <td style={{ padding: 8, border: '1px solid #eee' }}>{a.endDate}</td>
                <td style={{ padding: 8, border: '1px solid #eee' }}>{a.seller}</td>
                <td style={{ padding: 8, border: '1px solid #eee' }}><button style={{ background: '#222', color: '#fff', border: 'none', borderRadius: 4, padding: '4px 12px' }}>{a.action}</button></td>
              </tr>
            ))}
          </tbody>
        </table>
      </main>
    </div>
  );
} 