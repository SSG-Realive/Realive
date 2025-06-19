"use client";
import { useParams } from "next/navigation";
import { useState } from "react";

// 더미 데이터 (실제 프로젝트에서는 API 호출로 대체)
const dummyReviews = [
  { id: "1", product: "노트북", user: "user1", content: "좋아요!", date: "2024-06-01", status: "정상", productImage: "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=facearea&w=256&q=80" },
  { id: "2", product: "키보드", user: "user2", content: "별로예요", date: "2024-06-02", status: "신고됨", productImage: "https://images.unsplash.com/photo-1519389950473-47ba0277781c?auto=format&fit=facearea&w=256&q=80" },
  { id: "3", product: "마우스", user: "user3", content: "만족", date: "2024-06-03", status: "정상", productImage: "https://images.unsplash.com/photo-1519125323398-675f0ddb6308?auto=format&fit=facearea&w=256&q=80" },
  { id: "4", product: "모니터", user: "user4", content: "화질 좋아요", date: "2024-06-04", status: "정상", productImage: "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?auto=format&fit=facearea&w=256&q=80" },
  { id: "5", product: "의자", user: "user5", content: "편해요", date: "2024-06-05", status: "정상", productImage: "https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=facearea&w=256&q=80" },
  { id: "6", product: "책상", user: "user6", content: "튼튼합니다", date: "2024-06-06", status: "신고됨", productImage: "https://images.unsplash.com/photo-1465101046530-73398c7f28ca?auto=format&fit=facearea&w=256&q=80" },
  { id: "7", product: "스피커", user: "user7", content: "음질 좋아요", date: "2024-06-07", status: "정상", productImage: "https://images.unsplash.com/photo-1515378791036-0648a3ef77b2?auto=format&fit=facearea&w=256&q=80" },
  { id: "8", product: "프린터", user: "user8", content: "빠릅니다", date: "2024-06-08", status: "정상", productImage: "https://images.unsplash.com/photo-1464983953574-0892a716854b?auto=format&fit=facearea&w=256&q=80" },
  { id: "9", product: "마우스패드", user: "user9", content: "부드러워요", date: "2024-06-09", status: "정상", productImage: "https://images.unsplash.com/photo-1518717758536-85ae29035b6d?auto=format&fit=facearea&w=256&q=80" },
  { id: "10", product: "램프", user: "user10", content: "밝아요", date: "2024-06-10", status: "신고됨", productImage: "https://images.unsplash.com/photo-1465101178521-c1a9136a3b99?auto=format&fit=facearea&w=256&q=80" },
];

export default function ReviewDetailPage() {
  const params = useParams();
  const { id } = params;

  // id로 해당 리뷰 찾기
  const review = dummyReviews.find(r => r.id === id);

  if (!review) {
    return <div style={{ padding: 32 }}>리뷰 정보를 찾을 수 없습니다.</div>;
  }

  return (
    <div style={{ padding: 32 }}>
      <h2 style={{ fontWeight: 'bold', fontSize: 24 }}>리뷰 상세 페이지</h2>
      <div style={{ width: 120, height: 120, background: '#ccc', borderRadius: 8, marginBottom: 24, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
        {review.productImage ? (
          <img src={review.productImage} alt="이미지" style={{ width: '100%', height: '100%', objectFit: 'cover', borderRadius: 8 }} />
        ) : '이미지'}
      </div>
      <p><b>상품명:</b> {review.product}</p>
      <p><b>작성자:</b> {review.user}</p>
      <p><b>내용:</b> {review.content}</p>
      <p><b>작성일:</b> {review.date}</p>
      <p><b>상태:</b> {review.status}</p>
    </div>
  );
} 