import React from 'react';

const reviews = [
  { id: 1, date: '2024-06-10', author: 'Lee Sun', product: '의자', rating: 5, status: '노출', action: 'View' },
  { id: 2, date: '2024-06-09', author: 'Park Dong-min', product: '책상', rating: 4, status: '노출', action: 'View' },
  { id: 3, date: '2024-06-08', author: 'Kim Young-hee', product: '소파', rating: 3, status: '비노출', action: 'View' },
];

export default function SellerReviewPage() {
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
            <li style={{ margin: '16px 0' }}>경매</li>
            <li style={{ margin: '16px 0' }}>리뷰</li>
            <li style={{ margin: '16px 0', fontWeight: 'bold' }}>판매자리뷰</li>
            <li style={{ margin: '16px 0' }}>FAQ</li>
          </ul>
        </nav>
      </aside>
      {/* 메인 */}
      <main style={{ flex: 1, background: '#fff', padding: 32 }}>
        <header style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
          <h2 style={{ fontWeight: 'bold', fontSize: 24 }}>판매자 리뷰</h2>
          <span style={{ fontWeight: 'bold' }}>admin</span>
        </header>
        {/* 검색/필터 */}
        <div style={{ marginBottom: 16, display: 'flex', gap: 8 }}>
          <input placeholder="작성자, 상품명, 상태 검색" style={{ padding: 4, border: '1px solid #ccc', borderRadius: 4 }} />
          <select style={{ padding: 4, border: '1px solid #ccc', borderRadius: 4 }}>
            <option>상태 전체</option>
            <option>노출</option>
            <option>비노출</option>
          </select>
          <button style={{ padding: '4px 16px', background: '#222', color: '#fff', border: 'none', borderRadius: 4 }}>검색</button>
        </div>
        {/* 테이블 */}
        <table style={{ width: '100%', borderCollapse: 'collapse', background: '#fff', fontSize: 15 }}>
          <thead>
            <tr style={{ background: '#f7f7f7' }}>
              <th style={{ padding: 8, border: '1px solid #eee' }}>날짜</th>
              <th style={{ padding: 8, border: '1px solid #eee' }}>작성자</th>
              <th style={{ padding: 8, border: '1px solid #eee' }}>상품명</th>
              <th style={{ padding: 8, border: '1px solid #eee' }}>평점</th>
              <th style={{ padding: 8, border: '1px solid #eee' }}>상태</th>
              <th style={{ padding: 8, border: '1px solid #eee' }}>Action</th>
            </tr>
          </thead>
          <tbody>
            {reviews.map((r) => (
              <tr key={r.id}>
                <td style={{ padding: 8, border: '1px solid #eee' }}>{r.date}</td>
                <td style={{ padding: 8, border: '1px solid #eee' }}>{r.author}</td>
                <td style={{ padding: 8, border: '1px solid #eee' }}>{r.product}</td>
                <td style={{ padding: 8, border: '1px solid #eee' }}>{'★'.repeat(r.rating)}</td>
                <td style={{ padding: 8, border: '1px solid #eee' }}>
                  <span style={{ background: r.status === '노출' ? '#ffd600' : '#bdbdbd', color: '#222', padding: '2px 10px', borderRadius: 4, fontWeight: 'bold' }}>{r.status}</span>
                </td>
                <td style={{ padding: 8, border: '1px solid #eee' }}><button style={{ background: '#222', color: '#fff', border: 'none', borderRadius: 4, padding: '4px 12px' }}>{r.action}</button></td>
              </tr>
            ))}
          </tbody>
        </table>
      </main>
    </div>
  );
} 