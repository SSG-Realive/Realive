"use client";
import React, { useState, useEffect } from "react";
import apiClient from '@/lib/apiClient';

interface Seller {
  id: number;
  name: string;
  email: string;
  businessNumber?: string;
  joinedAt?: string;
  status: string;
}

export default function SellerManagementPage() {
  const [filter, setFilter] = useState("");
  const [selected, setSelected] = useState<Seller | null>(null);
  const [sellers, setSellers] = useState<Seller[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setLoading(true);
    const params = new URLSearchParams({
      userType: 'SELLER',
      page: '0',
      size: '100',
    });
    apiClient.get(`/admin/users?${params.toString()}`)
      .then(res => {
        setSellers(res.data.data.content || []);
        setLoading(false);
      })
      .catch(() => setLoading(false));
  }, []);

  const filtered = sellers.filter(s =>
    (s.name?.includes(filter) || s.email?.includes(filter) || s.businessNumber?.includes(filter))
  );

  return (
    <div className="p-8">
      <div className="flex justify-between items-center mb-4">
        <h1 className="text-2xl font-bold">판매자</h1>
        <button
          className="bg-green-500 text-white px-4 py-2 rounded"
          onClick={() => alert('판매자 승인처리 기능(추후 구현)')}
        >
          판매자 승인처리
        </button>
      </div>
      <div className="mb-4">
        <input
          type="text"
          placeholder="이름/이메일/사업자번호 검색"
          value={filter}
          onChange={e => setFilter(e.target.value)}
          className="border rounded px-3 py-2"
        />
      </div>
      {loading ? (
        <div>로딩 중...</div>
      ) : (
        <table className="min-w-full border text-sm">
          <thead>
            <tr className="bg-gray-100">
              <th className="px-4 py-2">이름</th>
              <th className="px-4 py-2">이메일</th>
              <th className="px-4 py-2">사업자번호</th>
              <th className="px-4 py-2">가입일</th>
              <th className="px-4 py-2">상태</th>
              <th className="px-4 py-2">상세</th>
            </tr>
          </thead>
          <tbody>
            {filtered.map(s => (
              <tr key={s.id}>
                <td className="px-4 py-2">{s.name}</td>
                <td className="px-4 py-2">{s.email}</td>
                <td className="px-4 py-2">{s.businessNumber || '-'}</td>
                <td className="px-4 py-2">{s.joinedAt || '-'}</td>
                <td className="px-4 py-2">{s.status}</td>
                <td className="px-4 py-2">
                  <button className="text-blue-600 underline" onClick={() => setSelected(s)}>
                    상세
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
      {selected && (
        <div className="fixed inset-0 bg-black bg-opacity-30 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-8 min-w-[300px]">
            <h2 className="text-xl font-bold mb-4">판매자 상세</h2>
            <p><b>이름:</b> {selected.name}</p>
            <p><b>이메일:</b> {selected.email}</p>
            <p><b>사업자번호:</b> {selected.businessNumber || '-'}</p>
            <p><b>가입일:</b> {selected.joinedAt || '-'}</p>
            <p><b>상태:</b> {selected.status}</p>
            <button className="mt-4 px-4 py-2 bg-blue-500 text-white rounded" onClick={() => setSelected(null)}>
              닫기
            </button>
          </div>
        </div>
      )}
    </div>
  );
} 