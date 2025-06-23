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
    <div className="p-8">
      <h2 className="text-2xl font-bold mb-6">사용자 패널티 목록</h2>
        <input
          type="text"
          placeholder="사용자/사유 검색"
          value={search}
          onChange={e => setSearch(e.target.value)}
        className="border rounded px-3 py-2 mb-4"
        />
      <table className="min-w-full border text-sm">
        <thead>
          <tr className="bg-gray-100">
            <th className="px-2 py-1">번호</th>
            <th className="px-2 py-1">User</th>
            <th className="px-2 py-1">사유</th>
            <th className="px-2 py-1">일자</th>
            <th className="px-2 py-1">View</th>
          </tr>
        </thead>
        <tbody>
          {filtered.map((p, idx) => (
            <tr key={p.id}>
              <td className="px-2 py-1" style={{ textAlign: 'center' }}>{idx + 1}</td>
              <td className="px-2 py-1">{getCustomerName(p.customerId)}</td>
              <td className="px-2 py-1">{p.reason}</td>
              <td className="px-2 py-1">{p.date}</td>
              <td className="px-2 py-1"><a href={`/admin/customers/penalty/${p.id}`}>View</a></td>
            </tr>
          ))}
        </tbody>
      </table>
      <button
        className="mt-4 px-4 py-2 bg-red-500 text-white rounded"
        onClick={() => router.push('/admin/customers/penalty/register')}
      >
        사용자 패널티 등록
      </button>
    </div>
  );
}