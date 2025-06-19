"use client";
import React, { useState, useEffect } from "react";
import { useRouter } from 'next/navigation';
import apiClient from '@/lib/apiClient';

interface Customer {
  id: number;
  name: string;
  email: string;
  is_active: boolean;
  image?: string;
}

function CustomerListPage() {
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [search, setSearch] = useState("");
  const [status, setStatus] = useState("");
  const [loading, setLoading] = useState(true);
  const router = useRouter();

  if (typeof window !== 'undefined' && !localStorage.getItem('adminToken')) {
    window.location.replace('/admin/login');
    return null;
  }

  useEffect(() => {
    setLoading(true);
    const params = new URLSearchParams({
      userType: 'CUSTOMER',
      page: '0',
      size: '100', // 필요에 따라 조정
    });
    if (search) params.append('searchTerm', search);
    if (status) params.append('isActive', status === 'Active' ? 'true' : 'false');

    const token = typeof window !== 'undefined' ? localStorage.getItem('adminToken') : '';
    apiClient.get(`/admin/users?${params.toString()}`, {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      }
    })
      .then(res => {
        const customersWithBoolean = (res.data.data.content || []).map(c => ({
          ...c,
          is_active: c.is_active === true || c.is_active === 'true' || c.is_active === 1 || c.isActive === true || c.isActive === 'true' || c.isActive === 1
        }));
        setCustomers(customersWithBoolean);
        setLoading(false);
      })
      .catch(() => setLoading(false));
  }, [search, status]);

  // 고객 활성/비활성 토글
  const handleToggleActive = async (customer: Customer) => {
    const token = typeof window !== 'undefined' ? localStorage.getItem('adminToken') : '';
    try {
      await apiClient.put(`/admin/users/customers/${customer.id}/status`, {
        isActive: !customer.is_active
      }, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        }
      });
      alert(`고객 ${customer.name}의 상태가 변경되었습니다.`);
      setCustomers(prev =>
        prev.map(c =>
          c.id === customer.id ? { ...c, is_active: !c.is_active } : c
        )
      );
    } catch (err) {
      alert('상태 변경 실패: ' + (err?.response?.data?.message || err?.message || '알 수 없는 오류'));
    }
  };

  return (
    <div className="p-8">
      <div className="flex justify-between items-center mb-4">
        <h1 className="text-2xl font-bold">고객 관리</h1>
        <div className="text-lg font-semibold text-purple-700">
          총 고객: {customers.length}명
        </div>
      </div>
      <div className="mb-4 flex gap-2">
        <input
          type="text"
          placeholder="이름/이메일 검색"
          value={search}
          onChange={e => setSearch(e.target.value)}
          className="border rounded px-3 py-2"
        />
        <select
          value={status}
          onChange={e => setStatus(e.target.value)}
          className="border rounded px-3 py-2"
        >
          <option value="">전체</option>
          <option value="Active">Active</option>
          <option value="Blocked">Blocked</option>
        </select>
      </div>
      {loading ? (
        <div>로딩 중...</div>
      ) : (
        <table className="min-w-full border text-sm">
          <thead>
            <tr>
              <th>번호</th>
              <th>사진</th>
              <th>이름</th>
              <th>이메일</th>
              <th>상태</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            {customers.map((c, idx) => (
              <tr key={c.id}>
                <td style={{ textAlign: 'center' }}>{idx + 1}</td>
                <td className="px-2 py-1"><img src={c.image || '/public/images/placeholder.png'} alt="customer" className="w-9 h-9 rounded-full object-cover" /></td>
                <td className="px-2 py-1">{c.name}</td>
                <td className="px-2 py-1">{c.email}</td>
                <td className="px-2 py-1">{c.is_active ? 'Active' : 'Blocked'}</td>
                <td className="px-2 py-1">
                  <button
                    style={{
                      background: c.is_active ? '#f44336' : '#4caf50',
                      color: '#fff',
                      padding: '4px 12px',
                      borderRadius: 4,
                      border: 'none',
                      fontWeight: 'bold',
                      marginLeft: 8
                    }}
                    onClick={() => handleToggleActive(c)}
                  >
                    {c.is_active ? '정지' : '복구'}
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}

export default Object.assign(CustomerListPage, { pageTitle: '고객 관리' }); 