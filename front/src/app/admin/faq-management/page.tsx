"use client";
import React, { useState } from "react";

interface Faq {
  id: number;
  title: string;
  category: string;
  date: string;
}

const dummyFaqs: Faq[] = [
  { id: 1, title: "배송비는 얼마인가요?", category: "배송", date: "2024-06-01" },
  { id: 2, title: "환불은 어떻게 하나요?", category: "결제", date: "2024-06-02" },
];

export default function FaqManagementPage() {
  const [search, setSearch] = useState("");
  const [category, setCategory] = useState("");
  const [selected, setSelected] = useState<Faq | null>(null);

  const filtered = dummyFaqs.filter(f =>
    (f.title.includes(search) || f.category.includes(search)) &&
    (!category || f.category === category)
  );

  return (
    <div className="p-8">
      <h1 className="text-2xl font-bold mb-4">FAQ 관리</h1>
      <div className="flex gap-2 mb-4">
        <input
          type="text"
          placeholder="제목 검색"
          value={search}
          onChange={e => setSearch(e.target.value)}
          className="border rounded px-3 py-2"
        />
        <select
          value={category}
          onChange={e => setCategory(e.target.value)}
          className="border rounded px-3 py-2"
        >
          <option value="">All</option>
          <option value="배송">배송</option>
          <option value="결제">결제</option>
          <option value="회원">회원</option>
        </select>
        <button className="bg-blue-500 text-white px-4 py-2 rounded">Search</button>
      </div>
      <table className="min-w-full border text-sm">
        <thead>
          <tr className="bg-gray-100">
            <th className="px-2 py-2">제목</th>
            <th className="px-2 py-2">카테고리</th>
            <th className="px-2 py-2">날짜</th>
            <th className="px-2 py-2">수정</th>
            <th className="px-2 py-2">액션</th>
          </tr>
        </thead>
        <tbody>
          {filtered.map(f => (
            <tr key={f.id}>
              <td className="px-2 py-2">{f.title}</td>
              <td className="px-2 py-2">{f.category}</td>
              <td className="px-2 py-2">{f.date}</td>
              <td className="px-2 py-2">
                <button className="bg-yellow-400 text-white px-2 py-1 rounded mr-2">수정</button>
              </td>
              <td className="px-2 py-2">
                <button className="text-blue-600 underline mr-2" onClick={() => setSelected(f)}>상세</button>
                <button className="bg-red-500 text-white px-2 py-1 rounded">삭제</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      {selected && (
        <div className="fixed inset-0 bg-black bg-opacity-30 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-8 min-w-[300px]">
            <h2 className="text-xl font-bold mb-4">FAQ 상세</h2>
            <p><b>제목:</b> {selected.title}</p>
            <p><b>카테고리:</b> {selected.category}</p>
            <p><b>날짜:</b> {selected.date}</p>
            <button className="mt-4 px-4 py-2 bg-blue-500 text-white rounded" onClick={() => setSelected(null)}>닫기</button>
          </div>
        </div>
      )}
    </div>
  );
} 