"use client";
import React, { useState } from "react";

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
  { id: 3, user: "user6", title: "제품 사용법 문의", created: "2024-06-03", answered: true },
  { id: 4, user: "user7", title: "A/S 신청 방법?", created: "2024-06-04", answered: false },
  { id: 5, user: "user8", title: "재입고 일정", created: "2024-06-05", answered: true },
  { id: 6, user: "user9", title: "쿠폰 적용 문의", created: "2024-06-06", answered: false },
  { id: 7, user: "user10", title: "제품 호환성 문의", created: "2024-06-07", answered: true },
  { id: 8, user: "user11", title: "배송지 변경", created: "2024-06-08", answered: false },
  { id: 9, user: "user12", title: "결제 오류", created: "2024-06-09", answered: true },
  { id: 10, user: "user13", title: "포인트 적립 문의", created: "2024-06-10", answered: false },
];

export default function QnaManagementPage() {
  const [qnaSearch, setQnaSearch] = useState("");
  const [selectedQna, setSelectedQna] = useState<Qna | null>(null);
  const filteredQna = dummyQna.filter(q => q.title.includes(qnaSearch) || q.user.includes(qnaSearch));

  return (
    <div className="p-8">
      <h2 className="text-lg font-bold mb-4">Q&A 관리</h2>
      <form className="flex gap-2 mb-2">
        <input className="border px-2 py-1 flex-1" placeholder="제목/작성자 검색" value={qnaSearch} onChange={e => setQnaSearch(e.target.value)} />
        <button className="bg-blue-500 text-white px-4 py-2 rounded" type="button">Search</button>
      </form>
      <table className="min-w-full border text-sm">
        <thead>
          <tr className="bg-gray-100">
            <th className="px-2 py-1">번호</th>
            <th className="px-2 py-1">작성자</th>
            <th className="px-2 py-1">제목</th>
            <th className="px-2 py-1">작성일</th>
            <th className="px-2 py-1">답변여부</th>
            <th className="px-2 py-1">상세</th>
          </tr>
        </thead>
        <tbody>
          {filteredQna.map(q => (
            <tr key={q.id}>
              <td className="px-2 py-1">{q.id}</td>
              <td className="px-2 py-1">{q.user}</td>
              <td className="px-2 py-1">{q.title}</td>
              <td className="px-2 py-1">{q.created}</td>
              <td className="px-2 py-1">{q.answered ? "답변완료" : "미답변"}</td>
              <td className="px-2 py-1">
                <button className="text-blue-600 underline" onClick={() => setSelectedQna(q)}>상세</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      {selectedQna && (
        <div className="fixed inset-0 bg-black bg-opacity-30 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-8 min-w-[300px]">
            <h2 className="text-xl font-bold mb-4">Q&A 상세</h2>
            <p><b>제목:</b> {selectedQna.title}</p>
            <p><b>작성자:</b> {selectedQna.user}</p>
            <p><b>작성일:</b> {selectedQna.created}</p>
            <p><b>답변여부:</b> {selectedQna.answered ? "답변완료" : "미답변"}</p>
            <button className="mt-4 px-4 py-2 bg-blue-500 text-white rounded" onClick={() => setSelectedQna(null)}>닫기</button>
          </div>
        </div>
      )}
    </div>
  );
} 