"use client";
import React, { useState, useEffect } from "react";
import apiClient from "@/lib/apiClient";

interface Member {
  id: number;
  name: string;
  email: string;
  joinedAt: string;
  status: "활성" | "비활성";
  role: string;
}

const dummyMembers: Member[] = [
  { id: 1, name: "홍길동", email: "hong@test.com", joinedAt: "2024-03-01", status: "활성", role: "일반" },
  { id: 2, name: "김철수", email: "kim@test.com", joinedAt: "2024-03-10", status: "비활성", role: "관리자" },
];

export default function MemberManagementPage() {
  const [filter, setFilter] = useState("");
  const [selected, setSelected] = useState<Member | null>(null);
  const [totalMembers, setTotalMembers] = useState(0);

  useEffect(() => {
    // 전체 회원 수 가져오기
    const token = typeof window !== 'undefined' ? localStorage.getItem('adminToken') : '';
    apiClient.get('/admin/stats/members-period', {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      }
    })
    .then(res => {
      setTotalMembers(res.data.memberSummaryStats?.totalMembers || 0);
    })
    .catch(err => {
      console.error('Failed to fetch total members:', err);
    });
  }, []);

  const filtered = dummyMembers.filter(m => m.name.includes(filter) || m.email.includes(filter));

  return (
    <div className="p-8">
      <h1 className="text-2xl font-bold mb-4">회원</h1>
      
      {/* 전체 회원 수 카드 추가 */}
      <div className="mb-6">
        <div className="bg-white rounded shadow p-6">
          <h3 className="text-lg font-bold mb-2">전체 회원</h3>
          <div className="text-3xl font-bold text-purple-600">{totalMembers}</div>
        </div>
      </div>

      <div className="mb-4">
        <input
          type="text"
          placeholder="이름/이메일 검색"
          value={filter}
          onChange={e => setFilter(e.target.value)}
          className="border rounded px-3 py-2"
        />
      </div>
      <table className="min-w-full border">
        <thead>
          <tr className="bg-gray-100">
            <th className="px-4 py-2">이름</th>
            <th className="px-4 py-2">이메일</th>
            <th className="px-4 py-2">가입일</th>
            <th className="px-4 py-2">상태</th>
            <th className="px-4 py-2">역할</th>
            <th className="px-4 py-2">상세</th>
          </tr>
        </thead>
        <tbody>
          {filtered.map(m => (
            <tr key={m.id}>
              <td className="px-4 py-2">{m.name}</td>
              <td className="px-4 py-2">{m.email}</td>
              <td className="px-4 py-2">{m.joinedAt}</td>
              <td className="px-4 py-2">{m.status}</td>
              <td className="px-4 py-2">{m.role}</td>
              <td className="px-4 py-2">
                <button className="text-blue-600 underline" onClick={() => setSelected(m)}>
                  상세
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      {selected && (
        <div className="fixed inset-0 bg-black bg-opacity-30 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-8 min-w-[300px]">
            <h2 className="text-xl font-bold mb-4">회원 상세</h2>
            <p><b>이름:</b> {selected.name}</p>
            <p><b>이메일:</b> {selected.email}</p>
            <p><b>가입일:</b> {selected.joinedAt}</p>
            <p><b>상태:</b> {selected.status}</p>
            <p><b>역할:</b> {selected.role}</p>
            <button className="mt-4 px-4 py-2 bg-blue-500 text-white rounded" onClick={() => setSelected(null)}>
              닫기
            </button>
          </div>
        </div>
      )}
    </div>
  );
} 