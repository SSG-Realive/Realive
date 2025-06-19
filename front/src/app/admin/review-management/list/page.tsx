"use client";
import React, { useState } from "react";
import { useRouter } from "next/navigation";

interface Review {
  id: number;
  product: string;
  user: string;
  content: string;
  date: string;
  status: "정상" | "신고됨";
  productImage: string; // 실제 상품 이미지
}

const dummyReviews: Review[] = [
  { id: 1, product: "노트북", user: "user1", content: "좋아요!", date: "2024-06-01", status: "정상", productImage: "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=facearea&w=256&q=80" },
  { id: 2, product: "키보드", user: "user2", content: "별로예요", date: "2024-06-02", status: "신고됨", productImage: "https://images.unsplash.com/photo-1519389950473-47ba0277781c?auto=format&fit=facearea&w=256&q=80" },
  { id: 3, product: "마우스", user: "user3", content: "만족", date: "2024-06-03", status: "정상", productImage: "https://images.unsplash.com/photo-1519125323398-675f0ddb6308?auto=format&fit=facearea&w=256&q=80" },
  { id: 4, product: "모니터", user: "user4", content: "화질 좋아요", date: "2024-06-04", status: "정상", productImage: "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?auto=format&fit=facearea&w=256&q=80" },
  { id: 5, product: "의자", user: "user5", content: "편해요", date: "2024-06-05", status: "정상", productImage: "https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=facearea&w=256&q=80" },
  { id: 6, product: "책상", user: "user6", content: "튼튼합니다", date: "2024-06-06", status: "신고됨", productImage: "https://images.unsplash.com/photo-1465101046530-73398c7f28ca?auto=format&fit=facearea&w=256&q=80" },
  { id: 7, product: "스피커", user: "user7", content: "음질 좋아요", date: "2024-06-07", status: "정상", productImage: "https://images.unsplash.com/photo-1515378791036-0648a3ef77b2?auto=format&fit=facearea&w=256&q=80" },
  { id: 8, product: "프린터", user: "user8", content: "빠릅니다", date: "2024-06-08", status: "정상", productImage: "https://images.unsplash.com/photo-1464983953574-0892a716854b?auto=format&fit=facearea&w=256&q=80" },
  { id: 9, product: "마우스패드", user: "user9", content: "부드러워요", date: "2024-06-09", status: "정상", productImage: "https://images.unsplash.com/photo-1518717758536-85ae29035b6d?auto=format&fit=facearea&w=256&q=80" },
  { id: 10, product: "램프", user: "user10", content: "밝아요", date: "2024-06-10", status: "신고됨", productImage: "https://images.unsplash.com/photo-1465101178521-c1a9136a3b99?auto=format&fit=facearea&w=256&q=80" },
];

function ReviewListPage() {
  const [search, setSearch] = useState("");
  const router = useRouter();
  const filtered = dummyReviews.filter(r => r.product.includes(search) || r.user.includes(search));

  return (
    <div className="p-8">
      <div className="mb-4">
        <input
          type="text"
          placeholder="상품명/작성자 검색"
          value={search}
          onChange={e => setSearch(e.target.value)}
          className="border rounded px-3 py-2"
        />
      </div>
      <div style={{ background: '#eee', padding: 16 }}>
        {filtered.map(r => (
          <div key={r.id} style={{
            display: 'flex', alignItems: 'center', background: '#fff', borderRadius: 8, marginBottom: 16, padding: 16, boxShadow: '0 1px 4px rgba(0,0,0,0.04)'
          }}>
            <div style={{ width: 80, height: 80, background: '#ccc', borderRadius: 4, display: 'flex', alignItems: 'center', justifyContent: 'center', marginRight: 24 }}>
              {r.productImage ? <img src={r.productImage} alt="이미지" style={{ width: '100%', height: '100%', objectFit: 'cover', borderRadius: 4 }} /> : '이미지'}
            </div>
            <div style={{ flex: 1 }}>
              <div style={{ fontWeight: 'bold', marginBottom: 4 }}>{r.content}</div>
              <div style={{ color: '#888', fontSize: 14 }}>{r.user} | {r.date}</div>
            </div>
            <button style={{ marginLeft: 16 }} onClick={() => router.push(`/admin/reviews/${r.id}`)}>View</button>
          </div>
        ))}
      </div>
    </div>
  );
}

export default Object.assign(ReviewListPage, { pageTitle: '리뷰 목록' }); 