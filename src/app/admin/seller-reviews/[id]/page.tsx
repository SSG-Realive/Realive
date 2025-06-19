"use client";
import { useParams, useRouter } from "next/navigation";
import { useEffect } from 'react';

// 더미 데이터
const dummySellerReviews = [
  { id: "1", product: "노트북", author: "user1", rating: 5, content: "좋은 상품이에요", status: "노출", createdAt: "2024-03-01", userImage: "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=facearea&w=256&q=80" },
  { id: "2", product: "키보드", author: "user2", rating: 4, content: "만족합니다", status: "노출", createdAt: "2024-03-02", userImage: "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=facearea&w=256&q=80" },
  { id: "3", product: "마우스", author: "user3", rating: 3, content: "보통이에요", status: "비노출", createdAt: "2024-03-03", userImage: "https://images.unsplash.com/photo-1599566150163-29194dcaad36?auto=format&fit=facearea&w=256&q=80" },
];

export default function SellerReviewDetailPage() {
  const params = useParams();
  const router = useRouter();
  const { id } = params;

  if (typeof window !== 'undefined' && !localStorage.getItem('adminToken')) {
    window.location.replace('/admin/login');
    return null;
  }

  const review = dummySellerReviews.find(r => r.id === id);

  if (!review) {
    return <div className="p-8">리뷰 정보를 찾을 수 없습니다.</div>;
  }

  return (
    <div className="p-8 max-w-2xl mx-auto">
      <h2 className="text-2xl font-bold mb-6">판매자 리뷰 상세</h2>
      <div className="bg-white rounded-lg p-6 shadow">
        <div className="flex items-center gap-4 mb-6">
          <img src={review.userImage} alt={review.author} className="w-16 h-16 rounded-full border" />
          <div>
            <p className="text-lg font-semibold">{review.author}</p>
            <p className="text-gray-500">작성자</p>
          </div>
        </div>
        <div className="space-y-4">
          <div>
            <p className="font-semibold">상품명</p>
            <p>{review.product}</p>
          </div>
          <div>
            <p className="font-semibold">평점</p>
            <p>{review.rating}점</p>
          </div>
          <div>
            <p className="font-semibold">내용</p>
            <p>{review.content}</p>
          </div>
          <div>
            <p className="font-semibold">상태</p>
            <p>{review.status}</p>
          </div>
          <div>
            <p className="font-semibold">작성일</p>
            <p>{review.createdAt}</p>
          </div>
        </div>
        <div className="mt-6 flex gap-2">
          <button 
            className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
            onClick={() => router.push('/admin/seller-reviews')}
          >
            목록으로
          </button>
        </div>
      </div>
    </div>
  );
} 