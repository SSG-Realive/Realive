"use client";
import { useRouter } from "next/navigation";
import { useState, useEffect } from "react";

const dummyReported = [
  { product: "키보드", user: "user2", reason: "욕설", status: "신고됨", userImage: "https://randomuser.me/api/portraits/women/52.jpg" },
  { product: "의자", user: "user5", reason: "광고성", status: "신고됨", userImage: "https://randomuser.me/api/portraits/men/55.jpg" },
  { product: "책상", user: "user6", reason: "도배", status: "신고됨", userImage: "https://randomuser.me/api/portraits/women/56.jpg" },
  { product: "마우스", user: "user3", reason: "부적절한 내용", status: "신고됨", userImage: "https://randomuser.me/api/portraits/men/53.jpg" },
  { product: "모니터", user: "user4", reason: "욕설", status: "신고됨", userImage: "https://randomuser.me/api/portraits/women/54.jpg" },
  { product: "노트북", user: "user1", reason: "도배", status: "신고됨", userImage: "https://randomuser.me/api/portraits/men/51.jpg" },
  { product: "스피커", user: "user7", reason: "욕설", status: "신고됨", userImage: "https://randomuser.me/api/portraits/men/57.jpg" },
  { product: "프린터", user: "user8", reason: "광고성", status: "신고됨", userImage: "https://randomuser.me/api/portraits/women/58.jpg" },
  { product: "마우스패드", user: "user9", reason: "도배", status: "신고됨", userImage: "https://randomuser.me/api/portraits/men/59.jpg" },
  { product: "램프", user: "user10", reason: "욕설", status: "신고됨", userImage: "https://randomuser.me/api/portraits/women/60.jpg" },
  { product: "노트북 파우치", user: "user11", reason: "욕설", status: "신고처리됨", userImage: "https://randomuser.me/api/portraits/men/61.jpg" },
  { product: "USB 허브", user: "user12", reason: "광고성", status: "신고처리됨", userImage: "https://randomuser.me/api/portraits/women/62.jpg" },
  { product: "스탠드", user: "user13", reason: "도배", status: "신고처리됨", userImage: "https://randomuser.me/api/portraits/men/63.jpg" },
];

type Reported = typeof dummyReported[0];

export default function ReviewReportedPage() {
  const router = useRouter();
  const [search, setSearch] = useState("");
  const filtered = dummyReported.filter(r => 
    r.product.includes(search) || 
    r.user.includes(search) || 
    r.reason.includes(search)
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
          placeholder="상품명/작성자/사유 검색"
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
            <th className="px-2 py-1">사유</th>
            <th className="px-2 py-1">상태</th>
            <th className="px-2 py-1">Actions</th>
          </tr>
        </thead>
        <tbody>
          {filtered.map((r, idx) => (
            <tr key={idx}>
              <td className="px-2 py-1">{r.product}</td>
              <td className="px-2 py-1">{r.user}</td>
              <td className="px-2 py-1">{r.reason}</td>
              <td className="px-2 py-1">{r.status}</td>
              <td className="px-2 py-1">
                <button 
                  className="text-blue-600 underline" 
                  onClick={() => router.push(`/admin/review-management/reported/${r.id}`)}
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