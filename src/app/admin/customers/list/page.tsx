"use client";
import React, { useState, useEffect, useCallback, useMemo } from "react";
import { useRouter } from 'next/navigation';
import { adminApi } from '@/lib/apiClient';

interface Customer {
  id: number;
  name: string;
  email: string;
  is_active: boolean;
  image?: string;
}

interface ApiResponse {
  data: {
    data: {
      content: Customer[];
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

const CustomerListPage: React.FC = () => {
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [search, setSearch] = useState("");
  const [status, setStatus] = useState("");
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
  const fetchCustomers = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      
      const params = new URLSearchParams({
        userType: 'CUSTOMER',
        page: '0',
        size: '100',
      });
      
      if (search.trim()) params.append('searchTerm', search.trim());
      if (status) params.append('isActive', status === 'Active' ? 'true' : 'false');

      const response: ApiResponse = await adminApi.get(`/admin/users?${params.toString()}`);
      
      const customersWithBoolean = (response.data.data.content || []).map((c: any) => ({
        ...c,
        is_active: Boolean(c.is_active === true || c.is_active === 'true' || c.is_active === 1 || c.isActive === true || c.isActive === 'true' || c.isActive === 1)
      }));
      
      setCustomers(customersWithBoolean);
    } catch (err) {
      const error = err as ErrorResponse;
      setError(error?.response?.data?.message || error?.message || '데이터를 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  }, [search, status]);

  useEffect(() => {
    fetchCustomers();
  }, [fetchCustomers]);

  // 상태 토글 핸들러
  const handleToggleActive = useCallback(async (customer: Customer) => {
    try {
      setUpdatingId(customer.id);
      
      await adminApi.put(`/admin/users/customers/${customer.id}/status`, {
        isActive: !customer.is_active
      });
      
      setCustomers(prev =>
        prev.map(c =>
          c.id === customer.id ? { ...c, is_active: !c.is_active } : c
        )
      );
    } catch (err) {
      const error = err as ErrorResponse;
      alert(`상태 변경 실패: ${error?.response?.data?.message || error?.message || '알 수 없는 오류'}`);
    } finally {
      setUpdatingId(null);
    }
  }, []);

  // 필터링된 고객 목록
  const filteredCustomers = useMemo(() => {
    return customers.filter(customer => {
      const matchesSearch = !search.trim() || 
        customer.name.toLowerCase().includes(search.toLowerCase()) ||
        customer.email.toLowerCase().includes(search.toLowerCase());
      
      const matchesStatus = !status || 
        (status === 'Active' && customer.is_active) ||
        (status === 'Blocked' && !customer.is_active);
      
      return matchesSearch && matchesStatus;
    });
  }, [customers, search, status]);

  // 검색 디바운싱
  const handleSearchChange = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    setSearch(e.target.value);
  }, []);

  const handleStatusChange = useCallback((e: React.ChangeEvent<HTMLSelectElement>) => {
    setStatus(e.target.value);
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
              <h1 className="text-2xl font-bold text-gray-900">고객 관리</h1>
              <p className="text-sm text-gray-600 mt-1">고객 계정을 관리하고 상태를 변경할 수 있습니다.</p>
            </div>
            <div className="flex items-center gap-3">
              <div className="text-right">
                <div className="text-2xl font-bold text-purple-600">{filteredCustomers.length}</div>
                <div className="text-sm text-gray-500">총 고객</div>
              </div>
            </div>
          </div>
        </div>

        {/* 필터 */}
        <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
          <div className="flex flex-col sm:flex-row gap-4">
            <div className="flex-1">
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
            <div className="sm:w-48">
              <label htmlFor="status" className="block text-sm font-medium text-gray-700 mb-2">
                상태
              </label>
              <select
                id="status"
                value={status}
                onChange={handleStatusChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-purple-500"
              >
                <option value="">전체</option>
                <option value="Active">활성</option>
                <option value="Blocked">정지</option>
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
                  onClick={fetchCustomers}
                  className="px-4 py-2 bg-purple-600 text-white rounded-md hover:bg-purple-700 focus:outline-none focus:ring-2 focus:ring-purple-500"
                >
                  다시 시도
                </button>
              </div>
            </div>
          ) : filteredCustomers.length === 0 ? (
            <div className="flex items-center justify-center py-12">
              <div className="text-center">
                <div className="text-gray-500 text-lg mb-2">고객이 없습니다</div>
                <div className="text-gray-400 text-sm">
                  {search || status ? '검색 조건을 변경해보세요.' : '등록된 고객이 없습니다.'}
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
                      상태
                    </th>
                    <th className="px-3 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider w-24">
                      액션
                    </th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {filteredCustomers.map((customer, index) => (
                    <tr key={customer.id} className="hover:bg-gray-50 transition-colors">
                      <td className="px-3 py-4 whitespace-nowrap text-sm text-gray-900">
                        {index + 1}
                      </td>
                      <td className="px-3 py-4 whitespace-nowrap text-center">
                        <div className="flex justify-center items-center w-full">
                          <img
                            src={customer.image || '/images/default-profile.svg'}
                            alt={`${customer.name}의 프로필`}
                            className="h-8 w-8 rounded-full object-cover border-2 border-gray-200 mx-auto"
                            onError={(e) => {
                              (e.currentTarget as HTMLImageElement).src = '/images/placeholder.png';
                            }}
                          />
                        </div>
                      </td>
                      <td className="px-3 py-4 whitespace-nowrap">
                        <div className="text-sm font-medium text-gray-900 truncate max-w-[120px]" title={customer.name}>
                          {customer.name}
                        </div>
                      </td>
                      <td className="px-3 py-4 whitespace-nowrap">
                        <div className="text-sm text-gray-900 truncate max-w-[180px]" title={customer.email}>
                          {customer.email}
                        </div>
                      </td>
                      <td className="px-3 py-4 whitespace-nowrap text-center">
                        <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
                          customer.is_active
                            ? 'bg-green-100 text-green-800'
                            : 'bg-red-100 text-red-800'
                        }`}>
                          {customer.is_active ? '활성' : '정지'}
                        </span>
                      </td>
                      <td className="px-3 py-4 whitespace-nowrap text-center">
                        <button
                          onClick={() => handleToggleActive(customer)}
                          disabled={updatingId === customer.id}
                          className={`inline-flex items-center px-3 py-1 border border-transparent text-xs font-medium rounded-md focus:outline-none focus:ring-2 focus:ring-offset-2 transition-colors ${
                            customer.is_active
                              ? 'bg-red-600 hover:bg-red-700 focus:ring-red-500 text-white'
                              : 'bg-green-600 hover:bg-green-700 focus:ring-green-500 text-white'
                          } ${updatingId === customer.id ? 'opacity-50 cursor-not-allowed' : ''}`}
                        >
                          {updatingId === customer.id ? (
                            <>
                              <div className="animate-spin rounded-full h-3 w-3 border-b border-white mr-1"></div>
                              처리중
                            </>
                          ) : (
                            customer.is_active ? '정지' : '복구'
                          )}
                        </button>
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

export default Object.assign(CustomerListPage, { pageTitle: '고객 관리' });