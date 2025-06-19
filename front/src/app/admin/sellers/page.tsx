'use client';
import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import apiClient from '@/lib/apiClient';
import Link from 'next/link';

interface Seller {
  id: number;
  name: string;
  email: string;
  status: string;
  image?: string;
  is_approved: boolean;
  is_active: boolean;
}

export default function AdminSellersPage() {
  const [search, setSearch] = useState("");
  const [status, setStatus] = useState("");
  const [activeFilter, setActiveFilter] = useState('');
  const [sellers, setSellers] = useState<Seller[]>([]);
  const [loading, setLoading] = useState(true);
  const router = useRouter();

  if (typeof window !== 'undefined' && !localStorage.getItem('adminToken')) {
    window.location.replace('/admin/login');
    return null;
  }

  useEffect(() => {
    setLoading(true);
    const params = new URLSearchParams({
      userType: 'SELLER',
      page: '0',
      size: '100',
    });
    const token = typeof window !== 'undefined' ? localStorage.getItem('adminToken') : '';
    apiClient.get(`/admin/users?${params.toString()}`, {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      }
    })
      .then(res => {
        const sellersWithBoolean = (res.data.data.content || []).map(s => ({
          ...s,
          is_approved: s.is_approved === true || s.is_approved === 'true' || s.is_approved === 1 || s.isApproved === true || s.isApproved === 'true' || s.isApproved === 1,
          is_active: s.is_active === true || s.is_active === 'true' || s.is_active === 1 || s.isActive === true || s.isActive === 'true' || s.isActive === 1
        }));
        setSellers(sellersWithBoolean);
        setLoading(false);
      })
      .catch(() => setLoading(false));
  }, []);

  const filtered = sellers.filter(s =>
    (s.name?.includes(search) || s.email?.includes(search)) &&
    (!status || (s.is_approved ? '승인' : '승인처리전') === status) &&
    (!activeFilter || (activeFilter === 'active' ? s.is_active : activeFilter === 'inactive' ? !s.is_active : true))
  );

  // 판매자 활성/비활성 토글
  const handleToggleActive = async (seller: Seller) => {
    const token = typeof window !== 'undefined' ? localStorage.getItem('adminToken') : '';
    try {
      await apiClient.put(`/admin/users/sellers/${seller.id}/status`, {
        isActive: !seller.is_active
      }, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        }
      });
      alert(`판매자 ${seller.name}의 상태가 변경되었습니다.`);
      setSellers(prev =>
        prev.map(s =>
          s.id === seller.id ? { ...s, is_active: !s.is_active } : s
        )
      );
    } catch (err) {
      alert('상태 변경 실패: ' + (err?.response?.data?.message || err?.message || '알 수 없는 오류'));
    }
  };

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', gap: 8, alignItems: 'center', justifyContent: 'space-between' }}>
        <div style={{ display: 'flex', gap: 8 }}>
        <input
          type="text"
          placeholder="이름/이메일 검색"
          value={search}
          onChange={e => setSearch(e.target.value)}
          style={{ width: 200, padding: 4, border: '1px solid #ccc', borderRadius: 4 }}
        />
        <select
          value={status}
          onChange={e => setStatus(e.target.value)}
          style={{ padding: 4, border: '1px solid #ccc', borderRadius: 4 }}
        >
          <option value="">전체</option>
          <option value="승인">승인</option>
            <option value="승인처리전">승인처리전</option>
          </select>
          <select
            value={activeFilter}
            onChange={e => setActiveFilter(e.target.value)}
            style={{ padding: 4, border: '1px solid #ccc', borderRadius: 4 }}
          >
            <option value="">전체</option>
            <option value="active">활성</option>
            <option value="inactive">정지</option>
        </select>
        </div>
        <div style={{ color: 'purple', fontWeight: 'bold', fontSize: 18 }}>
          총 판매자: {filtered.length}명
        </div>
      </div>
      {loading ? (
        <div>로딩 중...</div>
      ) : (
      <table style={{ width: '100%', borderCollapse: 'collapse', marginTop: 24 }}>
        <thead>
          <tr style={{ background: '#f7f7f7' }}>
              <th style={{ padding: 8, border: '1px solid #eee' }}>번호</th>
            <th style={{ padding: 8, border: '1px solid #eee' }}>사진</th>
            <th style={{ padding: 8, border: '1px solid #eee' }}>이름</th>
            <th style={{ padding: 8, border: '1px solid #eee' }}>이메일</th>
              <th style={{ padding: 8, border: '1px solid #eee' }}>Status</th>
              <th style={{ padding: 8, border: '1px solid #eee' }}>요청</th>
              <th style={{ padding: 8, border: '1px solid #eee' }}>Action</th>
          </tr>
        </thead>
        <tbody>
            {filtered.map((s, idx) => (
            <tr key={s.id}>
                <td style={{ padding: 8, border: '1px solid #eee', textAlign: 'center' }}>{idx + 1}</td>
                <td style={{ padding: 8, border: '1px solid #eee' }}><img src={s.image || '/public/images/placeholder.png'} alt="seller" style={{ width: 36, height: 36, borderRadius: '50%', objectFit: 'cover' }} /></td>
              <td style={{ padding: 8, border: '1px solid #eee' }}>{s.name}</td>
              <td style={{ padding: 8, border: '1px solid #eee' }}>{s.email}</td>
              <td style={{ padding: 8, border: '1px solid #eee' }}>
                  <span style={{ background: s.is_approved ? '#1976d2' : '#ffa726', color: '#fff', padding: '2px 10px', borderRadius: 4, fontWeight: 'bold', fontSize: 14 }}>
                    {s.is_approved ? '승인' : '승인처리전'}
                  </span>
                </td>
                <td style={{ padding: 8, border: '1px solid #eee' }}>
                  {!s.is_approved && (
                    <>
                      <button style={{ background: '#4caf50', color: '#fff', padding: '4px 12px', borderRadius: 4, border: 'none', fontWeight: 'bold', marginRight: 8 }} onClick={() => alert(`${s.name} 승인! (추후 구현)`)}>
                        승인
                      </button>
                      <button style={{ background: '#f44336', color: '#fff', padding: '4px 12px', borderRadius: 4, border: 'none', fontWeight: 'bold' }} onClick={() => alert(`${s.name} 거절! (추후 구현)`)}>
                        거절
                      </button>
                    </>
                  )}
                </td>
                <td style={{ padding: 8, border: '1px solid #eee' }}>
                  <button
                    style={{
                      background: s.is_active ? '#f44336' : '#4caf50',
                      color: '#fff',
                      padding: '4px 12px',
                      borderRadius: 4,
                      border: 'none',
                      fontWeight: 'bold',
                      marginLeft: 8
                    }}
                    onClick={() => handleToggleActive(s)}
                  >
                    {s.is_active ? '정지' : '복구'}
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