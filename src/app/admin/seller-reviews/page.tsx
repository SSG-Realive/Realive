"use client";
import { useRouter } from "next/navigation";
import { useState } from "react";

interface SellerReview {
  id: number;
  product: string;
  author: string;
  rating: number;
  content: string;
  status: "정상" | "신고됨" | "삭제됨";
  createdAt: string;
}

const dummyReviews: SellerReview[] = [
  { id: 1, product: "노트북", author: "user1", rating: 5, content: "좋아요!", status: "정상", createdAt: "2024-06-01" },
  { id: 2, product: "키보드", author: "user2", rating: 2, content: "별로예요", status: "신고됨", createdAt: "2024-06-02" },
  { id: 3, product: "마우스", author: "user3", rating: 4, content: "만족", status: "정상", createdAt: "2024-06-03" },
];

export default function SellerReviewsPage() {
  const router = useRouter();
  const [search, setSearch] = useState("");
  const filtered = dummyReviews.filter(r => 
    r.product.includes(search) || 
    r.author.includes(search) || 
    r.content.includes(search)
  );

  return (
    <div className="p-8">
      <h1 className="text-2xl font-bold mb-4">판매자 리뷰 관리</h1>
      <div className="mb-4">
        <input
          type="text"
          placeholder="상품명/작성자/내용 검색"
          value={search}
          onChange={e => setSearch(e.target.value)}
          className="border rounded px-3 py-2"
        />
      </div>
      <table className="min-w-full border text-sm">
        <thead>
          <tr className="bg-gray-100">
            <th className="px-2 py-1">상품</th>
            <th className="px-2 py-1">작성자</th>
            <th className="px-2 py-1">평점</th>
            <th className="px-2 py-1">내용</th>
            <th className="px-2 py-1">상태</th>
            <th className="px-2 py-1">등록일</th>
            <th className="px-2 py-1">Actions</th>
          </tr>
        </thead>
        <tbody>
          {filtered.map(r => (
            <tr key={r.id}>
              <td className="px-2 py-1">{r.product}</td>
              <td className="px-2 py-1">{r.author}</td>
              <td className="px-2 py-1">{r.rating}점</td>
              <td className="px-2 py-1">{r.content}</td>
              <td className="px-2 py-1">{r.status}</td>
              <td className="px-2 py-1">{r.createdAt}</td>
              <td className="px-2 py-1">
                <button 
                  className="text-blue-600 underline" 
                  onClick={() => router.push(`/admin/seller-reviews/${r.id}`)}
                >
                  View
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
} 