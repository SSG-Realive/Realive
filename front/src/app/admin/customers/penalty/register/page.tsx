"use client";
import apiClient from '@/lib/apiClient';
import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";

export default function PenaltyRegisterPage() {
  const router = useRouter();
  const [customerId, setCustomerId] = useState("");
  const [reason, setReason] = useState("");
  const [points, setPoints] = useState(10);
  const [customers, setCustomers] = useState<{id: number, name: string, email: string}[]>([]);
  const [search, setSearch] = useState("");

  useEffect(() => {
    const token = typeof window !== 'undefined' ? localStorage.getItem('adminToken') : '';
    apiClient.get(`/admin/users?userType=CUSTOMER&page=0&size=100&searchTerm=${search}`,
      { headers: { 'Authorization': `Bearer ${token}` } })
      .then(res => setCustomers(res.data.data.content || []));
  }, [search]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const token = typeof window !== 'undefined' ? localStorage.getItem('adminToken') : '';
    try {
      await apiClient.post('/admin/penalties', {
        customerId: Number(customerId),
        reason,
        points,
        description: reason
      }, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        }
      });
      alert('패널티가 등록되었습니다.');
      router.push('/admin/customers/penalty');
      window.location.reload();
    } catch (err) {
      const error = err as any;
      alert('등록 실패: ' + (error?.response?.data?.message || error?.message || '알 수 없는 오류'));
    }
  };

  return (
    <div className="p-8 max-w-md mx-auto">
      <h2 className="text-lg font-bold mb-4">패널티 등록</h2>
      <form onSubmit={handleSubmit} className="flex flex-col gap-4">
        <input
          type="text"
          placeholder="고객 이름/이메일 검색"
          value={search}
          onChange={e => setSearch(e.target.value)}
          className="border rounded px-3 py-2"
        />
        <select
          value={customerId}
          onChange={e => setCustomerId(e.target.value)}
          className="border rounded px-3 py-2"
          required
        >
          <option value="">고객 선택</option>
          {customers.map(c => (
            <option key={c.id} value={c.id}>{c.name} ({c.email})</option>
          ))}
        </select>
        <input
          type="text"
          placeholder="사유"
          value={reason}
          onChange={e => setReason(e.target.value)}
          className="border rounded px-3 py-2"
          required
        />
        <input
          type="number"
          placeholder="포인트"
          value={points}
          onChange={e => setPoints(Number(e.target.value))}
          className="border rounded px-3 py-2"
          required
        />
        <button type="submit" className="bg-blue-500 text-white rounded px-4 py-2">등록</button>
        <button type="button" className="bg-gray-300 text-black rounded px-4 py-2" onClick={() => router.push('/admin/customers/penalty')}>취소</button>
      </form>
    </div>
  );
} 