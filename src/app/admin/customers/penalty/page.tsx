"use client";
import React, { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import apiClient from '@/lib/apiClient';

interface Penalty {
  id: string;
  customerId: number; // Long 타입 (백엔드에서 Long으로 변경됨)
  reason: string;
  date: string;
}

export default function PenaltyListPage() {
  const router = useRouter();
  const [penalties, setPenalties] = useState<Penalty[]>([]);
  const [customers, setCustomers] = useState<{id: number, name: string, email: string}[]>([]);
  const [search, setSearch] = useState("");

  useEffect(() => {
    const token = typeof window !== 'undefined' ? localStorage.getItem('adminToken') : '';
    apiClient.get('/admin/penalties?userType=CUSTOMER&page=0&size=100', {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      }
    })
      .then(res => {
        setPenalties(res.data.content || []);
      })
      .catch(() => setPenalties([]));
    apiClient.get('/admin/users?userType=CUSTOMER&page=0&size=100', {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      }
    })
      .then(res => setCustomers(res.data.data.content || []))
      .catch(() => setCustomers([]));
  }, []);

  const filtered = Array.isArray(penalties)
    ? penalties.filter(p => (p.customerId ?? '').toString().includes(search) || (p.reason ?? '').includes(search))
    : [];

  const getCustomerName = (customerId: number) => {
    const c = customers.find(c => c.id === customerId);
    return c ? `${c.name} (${c.email})` : customerId;
  };

  if (typeof window !== 'undefined' && !localStorage.getItem('adminToken')) {
    window.location.replace('/admin/login');
    return null;
  }

  return (
    <div className="w-full max-w-full min-h-screen bg-gray-50 p-2 sm:p-6 overflow-x-auto">
      <div className="w-full max-w-full">
        {/* 데스크탑 표 */}
        <div className="hidden md:block">
          <h2 className="text-2xl font-bold mb-6">사용자 패널티 목록</h2>
          <input
            type="text"
            placeholder="사용자/사유 검색"
            value={search}
            onChange={e => setSearch(e.target.value)}
            className="border rounded px-3 py-2 mb-4"
          />
          <div className="overflow-x-auto w-full">
            <table className="min-w-[900px] w-full border text-sm">
              <thead>
                <tr className="bg-gray-100">
                  <th className="whitespace-nowrap px-2 py-2 text-xs">번호</th>
                  <th className="whitespace-nowrap px-2 py-2 text-xs">User</th>
                  <th className="whitespace-nowrap px-2 py-2 text-xs">사유</th>
                  <th className="whitespace-nowrap px-2 py-2 text-xs">일자</th>
                  <th className="whitespace-nowrap px-2 py-2 text-xs">View</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((p, idx) => (
                  <tr key={p.id}>
                    <td className="whitespace-nowrap px-2 py-2 text-xs">{idx + 1}</td>
                    <td className="whitespace-nowrap px-2 py-2 text-xs">{getCustomerName(p.customerId)}</td>
                    <td className="whitespace-nowrap px-2 py-2 text-xs">{p.reason}</td>
                    <td className="whitespace-nowrap px-2 py-2 text-xs">{p.date}</td>
                    <td className="whitespace-nowrap px-2 py-2 text-xs"><a href={`/admin/customers/penalty/${p.id}`}>View</a></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <button
            className="mt-4 px-4 py-2 bg-red-500 text-white rounded"
            onClick={() => router.push('/admin/customers/penalty/register')}
          >
            사용자 패널티 등록
          </button>
        </div>
        {/* 모바일 카드형 */}
        <div className="block md:hidden space-y-4">
          {filtered.map((p, idx) => (
            <div key={p.id || idx} className="bg-white rounded shadow p-4">
              <div className="font-bold mb-2">번호: {idx + 1}</div>
              <div className="mb-1">User: {getCustomerName(p.customerId)}</div>
              <div className="mb-1">사유: {p.reason}</div>
              <div>
                <button
                  className="text-blue-600 underline"
                  onClick={() => router.push(`/admin/customers/penalty/${p.id}`)}
                >
                  상세 보기
                </button>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}