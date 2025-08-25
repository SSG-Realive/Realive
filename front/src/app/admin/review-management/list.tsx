"use client";
import { useRouter } from "next/navigation";
import { useState, useEffect } from "react";

interface Review {
  id: number;
  product: string;
  user: string;
  content: string;
  date: string;
  status: "정상" | "신고됨";
}

const dummyReviews: Review[] = [
  { id: 1, product: "노트북", user: "user1", content: "좋아요!", date: "2024-06-01", status: "정상" },
  { id: 2, product: "키보드", user: "user2", content: "별로예요", date: "2024-06-02", status: "신고됨" },
  { id: 3, product: "마우스", user: "user3", content: "만족", date: "2024-06-03", status: "정상" },
  { id: 4, product: "모니터", user: "user4", content: "화질 좋아요", date: "2024-06-04", status: "정상" },
  { id: 5, product: "의자", user: "user5", content: "편해요", date: "2024-06-05", status: "정상" },
  { id: 6, product: "책상", user: "user6", content: "튼튼합니다", date: "2024-06-06", status: "신고됨" },
  { id: 7, product: "스피커", user: "user7", content: "음질 좋아요", date: "2024-06-07", status: "정상" },
  { id: 8, product: "프린터", user: "user8", content: "빠릅니다", date: "2024-06-08", status: "정상" },
  { id: 9, product: "마우스패드", user: "user9", content: "부드러워요", date: "2024-06-09", status: "정상" },
  { id: 10, product: "램프", user: "user10", content: "밝아요", date: "2024-06-10", status: "신고됨" },
];

export default function ReviewListPage() {
  const router = useRouter();
  const [search, setSearch] = useState("");
  const filteredReviews = dummyReviews.filter(r => 
    r.product.includes(search) || 
    r.user.includes(search) || 
    r.content.includes(search)
  );

  if (typeof window !== 'undefined' && !localStorage.getItem('adminToken')) {
    window.location.replace('/admin/login');
    return null;
  }

  return (
    <div className="p-8">
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
            <th className="px-2 py-1">상품명</th>
            <th className="px-2 py-1">작성자</th>
            <th className="px-2 py-1">내용</th>
            <th className="px-2 py-1">작성일</th>
            <th className="px-2 py-1">상태</th>
            <th className="px-2 py-1">상세</th>
          </tr>
        </thead>
        <tbody>
          {filteredReviews.map(r => (
            <tr key={r.id}>
              <td className="px-2 py-1">{r.product}</td>
              <td className="px-2 py-1">{r.user}</td>
              <td className="px-2 py-1">{r.content}</td>
              <td className="px-2 py-1">{r.date}</td>
              <td className="px-2 py-1">{r.status}</td>
              <td className="px-2 py-1">
                <button 
                  className="text-blue-600 underline" 
                  onClick={() => router.push(`/admin/review-management/${r.id}`)}
                >
                  상세
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
} 