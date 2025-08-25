'use client';
import React, { useEffect, useState } from 'react';
import { useRouter } from "next/navigation";
import apiClient from '@/lib/apiClient';
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { ScrollArea, ScrollBar } from "@/components/ui/scroll-area";

interface Customer {
  id: number;
  name: string;
  email: string;
  status: boolean | string;
  image?: string;
}

interface Penalty {
  id: number;
  name?: string;
  user?: string;
  customerName?: string;
  reason?: string;
  cause?: string;
  description?: string;
  date?: string;
  createdAt?: string;
}

interface Seller {
  id: number;
  name: string;
  email: string;
  status: boolean | string;
  image?: string;
  businessNumber?: string;
  joinedAt?: string;
}

export default function AdminCustomersDashboard() {
  const router = useRouter();
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [sellers, setSellers] = useState<Seller[]>([]);
  const [loading, setLoading] = useState(true);
  const [penalties, setPenalties] = useState<Penalty[]>([]);
  const [totalMembers, setTotalMembers] = useState(0);
  const [tab, setTab] = useState<'CUSTOMER' | 'SELLER'>('CUSTOMER');

  useEffect(() => {
    if (typeof window !== 'undefined' && !localStorage.getItem('adminToken')) {
      window.location.replace('/admin/login');
      return;
    }
    setLoading(true);
    const token = typeof window !== 'undefined' ? localStorage.getItem('adminToken') : '';
    // 고객 목록
    apiClient.get(`/admin/users?userType=CUSTOMER&page=0&size=100`, {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      }
    })
      .then(res => {
        setCustomers(res.data.data.content || []);
        const customerCount = res.data.data.totalElements || (res.data.data.content?.length ?? 0);
        // 판매자 목록도 불러와서 합산
        apiClient.get(`/admin/users?userType=SELLER&page=0&size=100`, {
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json',
          }
        })
        .then(res2 => {
          setSellers(res2.data.data.content || []);
          const sellerCount = res2.data.data.totalElements || (res2.data.data.content?.length ?? 0);
          setTotalMembers(customerCount + sellerCount);
          setLoading(false);
        })
        .catch(() => {
          setSellers([]);
          setTotalMembers(customerCount);
          setLoading(false);
        });
      })
      .catch(() => {
        setCustomers([]);
        setSellers([]);
        setTotalMembers(0);
        setLoading(false);
      });
    // 패널티 목록 불러오기
    apiClient.get('/admin/penalties?userType=CUSTOMER&page=0&size=5', {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      }
    })
      .then(res => {
        // 콘솔로 실제 응답 구조 확인
        console.log('패널티 API 응답', res.data);
        // content, data.content 등 다양한 위치 대응
        const penaltiesArr = res.data?.data?.content || res.data?.content || [];
        setPenalties(penaltiesArr);
      })
      .catch(() => setPenalties([]));
  }, []);

  // 상태 한글화
  const getStatusLabel = (user: any) => {
    if (
      user.is_active === true || user.is_active === 'true' || user.is_active === 1 ||
      user.isActive === true || user.isActive === 'true' || user.isActive === 1 ||
      user.status === true || user.status === 'Active' || user.status === '활성'
    ) return '활성';
    return '비활성';
  };

  // 고객/판매자 활성/비활성 수
  const activeCustomers = customers.filter(c => getStatusLabel(c) === '활성').length;
  const inactiveCustomers = customers.filter(c => getStatusLabel(c) === '비활성').length;
  const activeSellers = sellers.filter(s => getStatusLabel(s) === '활성').length;
  const inactiveSellers = sellers.filter(s => getStatusLabel(s) === '비활성').length;

  // 전체 활성/비활성 회원 수
  const totalActive = activeCustomers + activeSellers;
  const totalInactive = inactiveCustomers + inactiveSellers;

  // 패널티에서 customerId/userId로 이름+이메일 찾기
  const getPenaltyUserName = (p: any) => {
    const id = p.customerId || p.userId;
    if (id && Array.isArray(customers)) {
      const c = customers.find(c => c.id === id);
      if (c) return `${c.name} (${c.email})`;
    }
    return p.name || p.user || p.customerName || '-';
  };

  return (
    <div className="p-8">
      <h2 className="text-2xl font-bold mb-4">회원 대시보드</h2>
      <div className="mb-6 grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="bg-white rounded shadow p-6">
          <h3 className="text-lg font-bold mb-2">전체 회원</h3>
          <div className="text-3xl font-bold">{totalMembers}</div>
        </div>
        <div className="bg-white rounded shadow p-6">
          <h3 className="text-lg font-bold mb-2">활성 회원</h3>
          <div className="text-3xl font-bold">{totalActive}</div>
        </div>
        <div className="bg-white rounded shadow p-6">
          <h3 className="text-lg font-bold mb-2">비활성 회원</h3>
          <div className="text-3xl font-bold">{totalInactive}</div>
        </div>
      </div>
      {/* 고객/판매자 관리 탭 */}
      <div className="mb-6 flex gap-4">
        <button
          className={`px-4 py-2 rounded font-bold ${tab === 'CUSTOMER' ? 'bg-blue-500 text-white' : 'bg-gray-200 text-gray-700'}`}
          onClick={() => setTab('CUSTOMER')}
        >
          고객 관리
        </button>
        <button
          className={`px-4 py-2 rounded font-bold ${tab === 'SELLER' ? 'bg-blue-500 text-white' : 'bg-gray-200 text-gray-700'}`}
          onClick={() => setTab('SELLER')}
        >
          판매자 관리
        </button>
      </div>
      <div className="grid grid-cols-1 gap-8">
        {/* 고객/판매자 관리 테이블 */}
        <div className="bg-white rounded shadow p-6 min-w-[280px] max-w-full">
          <h2 className="text-lg font-bold mb-4">{tab === 'CUSTOMER' ? '고객 관리' : '판매자 관리'}</h2>
          {loading ? (
            <div>로딩 중...</div>
          ) : (
            <div className="overflow-y-auto" style={{ maxHeight: 320 }}>
              <table className="min-w-full border text-sm">
                <thead>
                  <tr className="bg-gray-100">
                    <th className="px-2 py-1">이름</th>
                    <th className="px-2 py-1">이메일</th>
                    {tab === 'SELLER' && <th className="px-2 py-1">사업자번호</th>}
                    {tab === 'SELLER' && <th className="px-2 py-1">가입일</th>}
                    <th className="px-2 py-1">상태</th>
                  </tr>
                </thead>
                <tbody>
                  {(tab === 'CUSTOMER' ? customers.slice(0, 50) : sellers.slice(0, 50)).map(u => (
                    <tr key={u.id}>
                      <td className="px-2 py-1">{u.name}</td>
                      <td className="px-2 py-1">{u.email}</td>
                      {tab === 'SELLER' && <td className="px-2 py-1">{(u as Seller).businessNumber || '-'}</td>}
                      {tab === 'SELLER' && <td className="px-2 py-1">{(u as Seller).joinedAt || '-'}</td>}
                      <td className="px-2 py-1">{getStatusLabel(u)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
        {/* 사용자 패널티 - 하단으로 이동 */}
        <div className="bg-white rounded shadow p-6 min-w-[280px] max-w-full mt-8">
          <h2 className="text-lg font-bold mb-4">사용자 패널티</h2>
          <div style={{ border: '1px solid #eee' }}>
            <table className="min-w-full border text-sm">
              <thead>
                <tr className="bg-gray-100">
                  <th className="px-2 py-1">이름</th>
                  <th className="px-2 py-1">사유</th>
                  <th className="px-2 py-1">일자</th>
                </tr>
              </thead>
            </table>
            <div style={{ maxHeight: 200, overflowY: 'auto' }}>
              <table className="min-w-full border text-sm">
                <tbody>
                  {penalties.length === 0 ? (
                    <tr><td colSpan={3} className="text-center py-2">데이터 없음</td></tr>
                  ) : penalties.map((p, idx) => (
                    <tr key={p.id || idx}>
                      <td className="px-2 py-1">{getPenaltyUserName(p)}</td>
                      <td className="px-2 py-1">{p.reason || p.cause || p.description || '-'}</td>
                      <td className="px-2 py-1">{p.date || p.createdAt || '-'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
} 