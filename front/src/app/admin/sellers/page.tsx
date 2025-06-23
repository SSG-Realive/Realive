'use client';
import React, { useState, useEffect, useCallback, useMemo } from 'react';
import { useRouter } from 'next/navigation';
import { adminApi } from '@/lib/apiClient';

interface Seller {
  id: number;
  name: string;
  email: string;
  image?: string;
  is_approved: boolean;
  is_active: boolean;
}

interface ApiResponse {
  data: {
    data: {
      content: Seller[];
      totalElements: number;
      totalPages: number;
    };
  };
}

interface ErrorResponse {
  response?: {
    data?: {
      message?: string;
    };
  };
  message?: string;
}

const AdminSellersPage: React.FC = () => {
  const [sellers, setSellers] = useState<Seller[]>([]);
  const [search, setSearch] = useState('');
  const [status, setStatus] = useState('');
  const [activeFilter, setActiveFilter] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [updatingId, setUpdatingId] = useState<number | null>(null);
  const router = useRouter();

  // 인증 체크
  useEffect(() => {
    if (typeof window !== 'undefined' && !localStorage.getItem('adminToken')) {
      router.replace('/admin/login');
      return;
    }
  }, [router]);

  // 데이터 로딩
  const fetchSellers = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      
      const params = new URLSearchParams({
        userType: 'SELLER',
        page: '0',
        size: '100',
      });

      const response: ApiResponse = await adminApi.get(`/admin/users?${params.toString()}`);
      
      const sellersWithBoolean = (response.data.data.content || []).map((s: any) => ({
        ...s,
        is_approved: Boolean(s.is_approved === true || s.is_approved === 'true' || s.is_approved === 1 || s.isApproved === true || s.isApproved === 'true' || s.isApproved === 1),
        is_active: Boolean(s.is_active === true || s.is_active === 'true' || s.is_active === 1 || s.isActive === true || s.isActive === 'true' || s.isActive === 1)
      }));
      
      setSellers(sellersWithBoolean);
    } catch (err) {
      const error = err as ErrorResponse;
      setError(error?.response?.data?.message || error?.message || '데이터를 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchSellers();
  }, [fetchSellers]);

  // 필터링된 판매자 목록
  const filteredSellers = useMemo(() => {
    return sellers.filter(seller => {
      const matchesSearch = !search.trim() || 
        seller.name.toLowerCase().includes(search.toLowerCase()) ||
        seller.email.toLowerCase().includes(search.toLowerCase());
      
      const matchesStatus = !status || 
        (status === '승인' && seller.is_approved) ||
        (status === '승인처리전' && !seller.is_approved);
      
      const matchesActiveFilter = !activeFilter || 
        (activeFilter === 'active' && seller.is_active) ||
        (activeFilter === 'inactive' && !seller.is_active);
      
      return matchesSearch && matchesStatus && matchesActiveFilter;
    });
  }, [sellers, search, status, activeFilter]);

  // 상태 토글 핸들러
  const handleToggleActive = useCallback(async (seller: Seller) => {
    try {
      setUpdatingId(seller.id);
      
      await adminApi.put(`/admin/users/sellers/${seller.id}/status`, {
        isActive: !seller.is_active
      });
      
      setSellers(prev =>
        prev.map(s =>
          s.id === seller.id ? { ...s, is_active: !s.is_active } : s
        )
      );
    } catch (err) {
      const error = err as ErrorResponse;
      alert(`상태 변경 실패: ${error?.response?.data?.message || error?.message || '알 수 없는 오류'}`);
    } finally {
      setUpdatingId(null);
    }
  }, []);

  // 승인/거절 핸들러
  const handleApproval = useCallback(async (seller: Seller, approved: boolean) => {
    try {
      setUpdatingId(seller.id);
      
      await adminApi.post('/admin/sellers/approve', {
        sellerId: seller.id,
        approve: approved
      });
      
      setSellers(prev =>
        prev.map(s =>
          s.id === seller.id ? { ...s, is_approved: approved } : s
        )
      );
      
      alert(`${seller.name} ${approved ? '승인' : '거절'} 처리되었습니다.`);
    } catch (err) {
      const error = err as ErrorResponse;
      alert(`처리 실패: ${error?.response?.data?.message || error?.message || '알 수 없는 오류'}`);
    } finally {
      setUpdatingId(null);
    }
  }, []);

  // 이벤트 핸들러
  const handleSearchChange = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    setSearch(e.target.value);
  }, []);

  const handleStatusChange = useCallback((e: React.ChangeEvent<HTMLSelectElement>) => {
    setStatus(e.target.value);
  }, []);

  const handleActiveFilterChange = useCallback((e: React.ChangeEvent<HTMLSelectElement>) => {
    setActiveFilter(e.target.value);
  }, []);

  if (typeof window !== 'undefined' && !localStorage.getItem('adminToken')) {
    return null;
  }

  return (
    <div className="min-h-screen bg-gray-50 p-6">
      <div className="max-w-7xl mx-auto">
        {/* 헤더 */}
        <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
          <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
            <div>
              <h1 className="text-2xl font-bold text-gray-900">판매자 관리</h1>
              <p className="text-sm text-gray-600 mt-1">판매자 계정을 관리하고 승인/거절 및 상태를 변경할 수 있습니다.</p>
            </div>
            <div className="flex items-center gap-3">
              <div className="text-right">
                <div className="text-2xl font-bold text-purple-600">{filteredSellers.length}</div>
                <div className="text-sm text-gray-500">총 판매자</div>
              </div>
            </div>
          </div>
        </div>

        {/* 필터 */}
        <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <div>
              <label htmlFor="search" className="block text-sm font-medium text-gray-700 mb-2">
                검색
              </label>
              <input
                id="search"
                type="text"
                placeholder="이름 또는 이메일로 검색"
                value={search}
                onChange={handleSearchChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-purple-500"
              />
            </div>
            <div>
              <label htmlFor="status" className="block text-sm font-medium text-gray-700 mb-2">
                승인 상태
              </label>
              <select
                id="status"
                value={status}
                onChange={handleStatusChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-purple-500"
              >
                <option value="">전체</option>
                <option value="승인">승인</option>
                <option value="승인처리전">승인처리전</option>
              </select>
            </div>
            <div>
              <label htmlFor="activeFilter" className="block text-sm font-medium text-gray-700 mb-2">
                활성 상태
              </label>
              <select
                id="activeFilter"
                value={activeFilter}
                onChange={handleActiveFilterChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-purple-500"
              >
                <option value="">전체</option>
                <option value="active">활성</option>
                <option value="inactive">정지</option>
              </select>
            </div>
          </div>
        </div>

        {/* 테이블 */}
        <div className="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
          {loading ? (
            <div className="flex items-center justify-center py-12">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-purple-600"></div>
              <span className="ml-3 text-gray-600">데이터를 불러오는 중...</span>
            </div>
          ) : error ? (
            <div className="flex items-center justify-center py-12">
              <div className="text-center">
                <div className="text-red-500 text-lg mb-2">오류가 발생했습니다</div>
                <div className="text-gray-600 mb-4">{error}</div>
                <button
                  onClick={fetchSellers}
                  className="px-4 py-2 bg-purple-600 text-white rounded-md hover:bg-purple-700 focus:outline-none focus:ring-2 focus:ring-purple-500"
                >
                  다시 시도
                </button>
              </div>
            </div>
          ) : filteredSellers.length === 0 ? (
            <div className="flex items-center justify-center py-12">
              <div className="text-center">
                <div className="text-gray-500 text-lg mb-2">판매자가 없습니다</div>
                <div className="text-gray-400 text-sm">
                  {search || status || activeFilter ? '검색 조건을 변경해보세요.' : '등록된 판매자가 없습니다.'}
                </div>
              </div>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-3 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider w-16">
                      번호
                    </th>
                    <th className="px-3 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider w-16">
                      프로필
                    </th>
                    <th className="px-3 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider w-32">
                      이름
                    </th>
                    <th className="px-3 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider w-48">
                      이메일
                    </th>
                    <th className="px-3 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider w-24">
                      승인 상태
                    </th>
                    <th className="px-3 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider w-24">
                      활성 상태
                    </th>
                    <th className="px-3 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider w-32">
                      액션
                    </th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {filteredSellers.map((seller, index) => (
                    <tr key={seller.id} className="hover:bg-gray-50 transition-colors">
                      <td className="px-3 py-4 whitespace-nowrap text-sm text-gray-900">
                        {index + 1}
                      </td>
                      <td className="px-3 py-4 whitespace-nowrap text-center">
                        <div className="flex justify-center items-center w-full">
                          <img
                            src={seller.image || '/images/default-profile.svg'}
                            alt={`${seller.name}의 프로필`}
                            className="h-8 w-8 rounded-full object-cover border-2 border-gray-200 mx-auto"
                            onError={(e) => {
                              (e.currentTarget as HTMLImageElement).src = '/images/placeholder.png';
                            }}
                          />
                        </div>
                      </td>
                      <td className="px-3 py-4 whitespace-nowrap">
                        <div className="text-sm font-medium text-gray-900 truncate max-w-[120px]" title={seller.name}>
                          {seller.name}
                        </div>
                      </td>
                      <td className="px-3 py-4 whitespace-nowrap">
                        <div className="text-sm text-gray-900 truncate max-w-[180px]" title={seller.email}>
                          {seller.email}
                        </div>
                      </td>
                      <td className="px-3 py-4 whitespace-nowrap text-center">
                        <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
                          seller.is_approved
                            ? 'bg-blue-100 text-blue-800'
                            : 'bg-yellow-100 text-yellow-800'
                        }`}>
                          {seller.is_approved ? '승인' : '승인처리전'}
                        </span>
                      </td>
                      <td className="px-3 py-4 whitespace-nowrap text-center">
                        <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
                          seller.is_active
                            ? 'bg-green-100 text-green-800'
                            : 'bg-red-100 text-red-800'
                        }`}>
                          {seller.is_active ? '활성' : '정지'}
                        </span>
                      </td>
                      <td className="px-3 py-4 whitespace-nowrap text-center">
                        <div className="flex flex-col gap-1">
                          {!seller.is_approved && (
                            <div className="flex gap-1 justify-center">
                              <button
                                onClick={() => handleApproval(seller, true)}
                                disabled={updatingId === seller.id}
                                className="px-2 py-1 bg-blue-600 hover:bg-blue-700 text-white text-xs rounded focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:opacity-50"
                              >
                                승인
                              </button>
                              <button
                                onClick={() => handleApproval(seller, false)}
                                disabled={updatingId === seller.id}
                                className="px-2 py-1 bg-orange-600 hover:bg-orange-700 text-white text-xs rounded focus:outline-none focus:ring-2 focus:ring-orange-500 disabled:opacity-50"
                              >
                                거절
                              </button>
                            </div>
                          )}
                          <button
                            onClick={() => handleToggleActive(seller)}
                            disabled={updatingId === seller.id}
                            className={`px-2 py-1 border border-transparent text-xs font-medium rounded focus:outline-none focus:ring-2 focus:ring-offset-2 transition-colors ${
                              seller.is_active
                                ? 'bg-red-600 hover:bg-red-700 focus:ring-red-500 text-white'
                                : 'bg-green-600 hover:bg-green-700 focus:ring-green-500 text-white'
                            } ${updatingId === seller.id ? 'opacity-50 cursor-not-allowed' : ''}`}
                          >
                            {updatingId === seller.id ? (
                              <>
                                <div className="animate-spin rounded-full h-2 w-2 border-b border-white mr-1"></div>
                                처리중
                              </>
                            ) : (
                              seller.is_active ? '정지' : '복구'
                            )}
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default Object.assign(AdminSellersPage, { pageTitle: '판매자 관리' }); 