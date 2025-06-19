"use client";
import React from 'react';
import { useRouter } from 'next/navigation';

const dummyReviews = [
  { id: 1, product: "노트북", user: "user1", content: "좋아요!", date: "2024-06-01", status: "정상", image: "https://randomuser.me/api/portraits/men/41.jpg" },
  { id: 2, product: "키보드", user: "user2", content: "별로예요", date: "2024-06-02", status: "신고됨", image: "https://randomuser.me/api/portraits/men/42.jpg" },
  { id: 3, product: "마우스", user: "user3", content: "만족", date: "2024-06-03", status: "정상", image: "https://randomuser.me/api/portraits/men/43.jpg" },
  { id: 4, product: "모니터", user: "user4", content: "화질 좋아요", date: "2024-06-04", status: "정상", image: "https://randomuser.me/api/portraits/men/44.jpg" },
  { id: 5, product: "의자", user: "user5", content: "편해요", date: "2024-06-05", status: "정상", image: "https://randomuser.me/api/portraits/men/45.jpg" },
];

const AdminReviewListPage = () => {
  const router = useRouter();
  return (
    <div>
      <h2 style={{ fontWeight: 'bold', fontSize: 28, marginBottom: 16 }}>리뷰 목록</h2>
      <div style={{ marginBottom: 16, display: 'flex', alignItems: 'center', gap: 8 }}>
        <input type="text" placeholder="검색어를 입력하세요" style={{ width: 300, marginRight: 8 }} />
        <button>검색</button>
        <select style={{ marginLeft: 8 }}>
          <option value="">전체</option>
          <option value="latest">최신순</option>
          <option value="like">좋아요순</option>
          <option value="report">신고많은순</option>
        </select>
      </div>
      <div style={{ background: '#eee', padding: 16 }}>
        {dummyReviews.map((r) => (
          <div key={r.id} style={{
            display: 'flex', alignItems: 'center', background: '#fff', borderRadius: 8, marginBottom: 16, padding: 16, boxShadow: '0 1px 4px rgba(0,0,0,0.04)'
          }}>
            <div style={{ width: 80, height: 80, background: '#ccc', borderRadius: 4, display: 'flex', alignItems: 'center', justifyContent: 'center', marginRight: 24 }}>
              {r.image ? <img src={r.image} alt="이미지" style={{ width: '100%', height: '100%', objectFit: 'cover', borderRadius: 4 }} /> : '이미지'}
            </div>
            <div style={{ flex: 1 }}>
              <div style={{ fontWeight: 'bold', marginBottom: 4 }}>{r.content}</div>
              <div style={{ color: '#888', fontSize: 14 }}>{r.user} | {r.date}</div>
            </div>
            <button style={{ marginLeft: 16 }} onClick={() => router.push(`/admin/reviews/${r.id}`)}>상세보기</button>
          </div>
        ))}
      </div>
    </div>
  );
};

export default AdminReviewListPage; 