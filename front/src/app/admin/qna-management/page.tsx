"use client";
import { useRouter } from "next/navigation";
import { useState } from "react";

interface Qna {
  id: number;
  user: string;
  title: string;
  created: string;
  answered: boolean;
}

const dummyQna: Qna[] = [
  { id: 1, user: "user4", title: "배송은 언제 오나요?", created: "2024-06-01", answered: true },
  { id: 2, user: "user5", title: "환불 문의", created: "2024-06-02", answered: false },
];

export default function QnaManagementPage() {
  const router = useRouter();
  const [search, setSearch] = useState("");
  const filtered = dummyQna.filter(q => 
    q.title.includes(search) || 
    q.user.includes(search)
  );

  return (
    <div className="p-8">
      <h1 className="text-2xl font-bold mb-4">Q&A 관리</h1>
      <div className="mb-4">
        <input
          type="text"
          placeholder="제목/작성자 검색"
          value={search}
          onChange={e => setSearch(e.target.value)}
          className="border rounded px-3 py-2"
        />
      </div>
      <table className="min-w-full border text-sm">
        <thead>
          <tr className="bg-gray-100">
            <th className="px-2 py-2">번호</th>
            <th className="px-2 py-2">작성자</th>
            <th className="px-2 py-2">제목</th>
            <th className="px-2 py-2">작성일</th>
            <th className="px-2 py-2">답변여부</th>
            <th className="px-2 py-2">상세</th>
            <th className="px-2 py-2">액션</th>
          </tr>
        </thead>
        <tbody>
          {filtered.map(q => (
            <tr key={q.id}>
              <td className="px-2 py-2">{q.id}</td>
              <td className="px-2 py-2">{q.user}</td>
              <td className="px-2 py-2">{q.title}</td>
              <td className="px-2 py-2">{q.created}</td>
              <td className="px-2 py-2">{q.answered ? "답변완료" : "미답변"}</td>
              <td className="px-2 py-2">
                <button 
                  className="text-blue-600 underline" 
                  onClick={() => router.push(`/admin/qna-management/${q.id}`)}
                >
                  상세
                </button>
              </td>
              <td className="px-2 py-2">
                <button className="bg-green-500 text-white px-2 py-1 rounded mr-2">답변</button>
                <button className="bg-red-500 text-white px-2 py-1 rounded">삭제</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
} 