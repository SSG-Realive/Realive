"use client";
import React, { useState, useEffect } from "react";
import { useRouter } from 'next/navigation';
import { adminApi } from '@/lib/apiClient';
import { Users, UserCheck, UserX, AlertTriangle } from 'lucide-react';
import { Badge } from "@/components/ui/badge";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";

interface Customer {
  id: number;
  name: string;
  email: string;
  is_active: boolean;
  image?: string;
  createdAt?: string;
  created?: string;
  penaltyScore?: number;
}

interface Seller {
  id: number;
  name: string;
  email: string;
  is_active: boolean;
  image?: string;
}



interface CustomerDetail {
  id: number;
  email: string;
  name: string;
  phone: string;
  address: string;
  isVerified: boolean;
  isActive: boolean;
  penaltyScore: number;
  birth: string;
  gender: string;
  signupMethod: string;
  createdAt: string;
  updatedAt: string;
}

interface ApiResponse {
  data: {
    data: {
      content: Customer[];
      totalElements: number;
      totalPages: number;
      number: number;
      last: boolean;
    };
  };
}

const CustomerListPage: React.FC = () => {
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [search, setSearch] = useState("");
  const [status, setStatus] = useState("all");
  const [penaltySort, setPenaltySort] = useState("all");
  const [loading, setLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [updatingId, setUpdatingId] = useState<number | null>(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [totalElements, setTotalElements] = useState(0);
  const [selectedCustomer, setSelectedCustomer] = useState<CustomerDetail | null>(null);
  const [isDetailModalOpen, setIsDetailModalOpen] = useState(false);
  const [detailLoading, setDetailLoading] = useState(false);
  const [totalStats, setTotalStats] = useState({
    total: 0,
    active: 0,
    inactive: 0
  });
  const router = useRouter();

  // 인증 체크
  useEffect(() => {
    if (typeof window !== 'undefined' && !localStorage.getItem('adminToken')) {
      router.replace('/admin/login');
      return;
    }
  }, [router]);

  // 고객 통계 데이터 로딩
  const fetchTotalStats = async () => {
    try {
      // 고객 통계만 가져오기
      const customerResponse = await adminApi.get('/admin/users?userType=CUSTOMER&page=0&size=1000');
      const customerData = customerResponse.data.data.content || [];
      
      // 고객 통계 계산
      const totalActive = customerData.filter((user: any) => 
        user.is_active === true || user.is_active === 'true' || user.is_active === 1 ||
        user.isActive === true || user.isActive === 'true' || user.isActive === 1
      ).length;
      
      setTotalStats({
        total: customerData.length,
        active: totalActive,
        inactive: customerData.length - totalActive
      });
    } catch (err) {
      console.error('고객 통계 로딩 실패:', err);
    }
  };



  // 패널티 뱃지 컴포넌트
  const getPenaltyBadge = (customer: Customer) => {
    const totalPoints = customer.penaltyScore || 0;
    
    if (totalPoints === 0) {
      return <Badge variant="secondary">패널티 없음</Badge>;
    } else if (totalPoints <= 20) {
      return <Badge className="bg-yellow-100 text-yellow-800 border-yellow-200">{totalPoints}점</Badge>;
    } else if (totalPoints <= 40) {
      return <Badge className="bg-orange-100 text-orange-800 border-orange-200">{totalPoints}점</Badge>;
    } else if (totalPoints <= 60) {
      return <Badge className="bg-red-100 text-red-800 border-red-200">{totalPoints}점</Badge>;
    } else if (totalPoints <= 80) {
      return <Badge className="bg-red-600 text-white border-red-700">{totalPoints}점</Badge>;
    } else {
      return <Badge className="bg-gray-900 text-white border-gray-800">{totalPoints}점</Badge>;
    }
  };

  // 패널티 점수에 따른 고객 필터링 및 정렬
  const getFilteredAndSortedCustomers = () => {
    // 서버에서 이미 필터링된 데이터를 받아오므로, 클라이언트에서는 필터링만 수행
    let filtered = [...customers];

    // 패널티 필터링 (클라이언트 사이드)
    if (penaltySort === 'has') {
      filtered = filtered.filter(c => {
        const penaltyPoints = c.penaltyScore || 0;
        return penaltyPoints > 0;
      });
    } else if (penaltySort === 'none') {
      filtered = filtered.filter(c => {
        const penaltyPoints = c.penaltyScore || 0;
        return penaltyPoints === 0;
      });
    }

    return filtered;
  };

  // 데이터 로딩
  const fetchCustomers = async (page = 0, reset = true) => {
    try {
      if (reset) {
        setLoading(true);
        setCurrentPage(0);
      } else {
        setLoadingMore(true);
      }
      setError(null);
      
      const params = new URLSearchParams({
        userType: 'CUSTOMER',
        page: page.toString(),
        size: '20',
      });
      
      if (search.trim()) params.append('searchTerm', search.trim());
      if (status !== 'all') params.append('isActive', status === 'Active' ? 'true' : 'false');
      // 패널티 정렬은 클라이언트에서 처리하므로 서버에 전송하지 않음

      const response: ApiResponse = await adminApi.get(`/admin/users?${params.toString()}`);
      
      const customersWithBoolean = (response.data.data.content || []).map((c: any) => {
        return {
        ...c,
          is_active: Boolean(
            c.is_active === true ||
            c.is_active === 'true' ||
            c.is_active === 1 ||
            c.isActive === true ||
            c.isActive === 'true' ||
            c.isActive === 1
          ),
          penaltyScore: c.penaltyScore || 0 // API에서 받아온 penaltyScore를 그대로 사용
        };
      });
      
      if (reset) {
        setCustomers(customersWithBoolean);
      } else {
        setCustomers(prev => [...prev, ...customersWithBoolean]);
      }
      
      setTotalElements(response.data.data.totalElements || 0);
      setHasMore(!response.data.data.last);
      setCurrentPage(page);
    } catch (err: any) {
      setError(err?.response?.data?.message || err?.message || '데이터를 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
      setLoadingMore(false);
    }
  };

  // 더 불러오기 버튼 핸들러
  const loadMore = () => {
    if (!loadingMore && hasMore) {
      fetchCustomers(currentPage + 1, false);
    }
  };

  // 초기 데이터 로딩
  useEffect(() => {
    fetchTotalStats();
  }, []);

  // 초기 로딩
  useEffect(() => {
    fetchCustomers(0, true);
  }, []);

  // 검색/필터 변경 시
  useEffect(() => {
    fetchCustomers(0, true);
  }, [search, status, penaltySort]);

  // 상세조회 핸들러
  const handleViewDetail = async (customerId: number) => {
    try {
      setDetailLoading(true);
      const response = await adminApi.get(`/admin/users/customers/${customerId}`);
      const customerDetail = response.data.data;
      setSelectedCustomer(customerDetail);
      setIsDetailModalOpen(true);
    } catch (err: any) {
      alert(`상세 정보 조회 실패: ${err?.response?.data?.message || err?.message || '알 수 없는 오류'}`);
    } finally {
      setDetailLoading(false);
    }
  };

  // 상태 토글 핸들러
  const handleToggleActive = async (customer: Customer) => {
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
      
      // 상태 변경 후 전체 통계 다시 로딩
      fetchTotalStats();
    } catch (err: any) {
      alert(`상태 변경 실패: ${err?.response?.data?.message || err?.message || '알 수 없는 오류'}`);
    } finally {
      setUpdatingId(null);
    }
  };

  // 현재 페이지 고객 통계 (필터링된 결과)
  const currentPageStats = {
    total: totalElements,
    active: customers.filter(c => c.is_active).length,
    inactive: customers.filter(c => !c.is_active).length
  };

  // 상태 뱃지 shadcn 적용
  const getStatusBadge = (isActive: boolean) => (
    <Badge variant={isActive ? "success" : "destructive"} className={isActive ? "" : "text-white"}>{isActive ? "활성" : "비활성"}</Badge>
  );

  if (typeof window !== 'undefined' && !localStorage.getItem('adminToken')) {
    return null;
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 to-slate-100">
      <div className="max-w-7xl mx-auto p-4 sm:p-6 lg:p-8">
        {/* 헤더 */}
        <div className="mb-8">
          <div className="flex items-center gap-3 mb-4">
            <div className="p-3 bg-indigo-600 rounded-xl">
              <Users className="w-8 h-8 text-white" />
            </div>
            <div>
              <h1 className="text-3xl font-bold text-gray-900">고객 관리</h1>
              <p className="text-gray-600 mt-1">고객 계정을 관리하고 상태를 변경할 수 있습니다</p>
            </div>
          </div>

          {/* 통계 카드 */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
            <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
              <div className="flex items-center">
                <div className="flex-shrink-0">
                  <div className="w-8 h-8 bg-blue-100 rounded-lg flex items-center justify-center">
                    <Users className="w-5 h-5 text-blue-600" />
                  </div>
                </div>
                <div className="ml-4">
                  <p className="text-sm font-medium text-gray-600">전체 고객</p>
                  <p className="text-2xl font-bold text-gray-900">{totalStats.total.toLocaleString()}</p>
                </div>
              </div>
            </div>
            
            <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
              <div className="flex items-center">
                <div className="flex-shrink-0">
                  <div className="w-8 h-8 bg-green-100 rounded-lg flex items-center justify-center">
                    <UserCheck className="w-5 h-5 text-green-600" />
                  </div>
                </div>
                <div className="ml-4">
                  <p className="text-sm font-medium text-gray-600">활성 고객</p>
                  <p className="text-2xl font-bold text-green-600">{totalStats.active.toLocaleString()}</p>
                </div>
              </div>
            </div>
            
            <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
              <div className="flex items-center">
                <div className="flex-shrink-0">
                  <div className="w-8 h-8 bg-red-100 rounded-lg flex items-center justify-center">
                    <UserX className="w-5 h-5 text-red-600" />
                  </div>
                </div>
                <div className="ml-4">
                  <p className="text-sm font-medium text-gray-600">정지 고객</p>
                  <p className="text-2xl font-bold text-red-600">{totalStats.inactive.toLocaleString()}</p>
                </div>
              </div>
            </div>

            <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
              <div className="flex items-center">
                <div className="flex-shrink-0">
                  <div className="w-8 h-8 bg-orange-100 rounded-lg flex items-center justify-center">
                    <AlertTriangle className="w-5 h-5 text-orange-600" />
                  </div>
                </div>
                <div className="ml-4">
                  <p className="text-sm font-medium text-gray-600">패널티 고객</p>
                  <p className="text-2xl font-bold text-orange-600">
                    {customers.filter(c => (c.penaltyScore || 0) > 0).length.toLocaleString()}
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* 검색/필터 영역 */}
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 mb-6">
          <div className="grid grid-cols-1 md:grid-cols-5 gap-4 items-end">
            <div className="md:col-span-2">
              <label className="block text-sm font-medium text-gray-700 mb-2">검색</label>
              <Input
                type="text"
                placeholder="이름, 이메일로 검색"
                value={search}
                onChange={e => setSearch(e.target.value)}
                className="w-full"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">활성 상태</label>
              <Select value={status} onValueChange={(value) => setStatus(value)}>
                <SelectTrigger className="w-full">
                  <SelectValue placeholder="활성 상태 선택" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">모든 상태</SelectItem>
                  <SelectItem value="Active">활성 고객</SelectItem>
                  <SelectItem value="Inactive">비활성 고객</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">패널티 상태</label>
              <Select value={penaltySort} onValueChange={(value) => setPenaltySort(value)}>
                <SelectTrigger className="w-full">
                  <SelectValue placeholder="패널티 상태 선택" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">모든 고객</SelectItem>
                  <SelectItem value="has">패널티 있음</SelectItem>
                  <SelectItem value="none">패널티 없음</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div>
              <Button 
                variant="outline" 
                onClick={() => fetchCustomers(0, true)} 
                disabled={loading}
                size="sm"
                className="w-full"
              >
                새로고침
              </Button>
            </div>
          </div>
        </div>

        {/* 테이블 */}
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
          {loading ? (
            <div className="flex items-center justify-center py-16">
              <div className="text-center">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600 mx-auto mb-4"></div>
                <p className="text-gray-600">고객 정보를 불러오는 중...</p>
              </div>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">이름</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">이메일</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">가입일</th>
                    <th className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">패널티</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">상태</th>
                    <th className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">정지</th>
                    <th className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">상세</th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {getFilteredAndSortedCustomers().map(customer => (
                    <tr key={customer.id} className="hover:bg-gray-50 transition-colors">
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex items-center">
                          <div className="flex-shrink-0 h-10 w-10">
                            <div className="h-10 w-10 rounded-full bg-gray-200 flex items-center justify-center">
                              <span className="text-sm font-medium text-gray-700">{customer.name?.charAt(0) || '?'}</span>
                            </div>
                          </div>
                          <div className="ml-4">
                            <div className="text-sm font-medium text-gray-900">{customer.name}</div>
                          </div>
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="text-sm text-gray-900">{customer.email}</div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="text-sm text-gray-900">
                          {(customer.createdAt || customer.created) ? 
                            new Date(customer.createdAt || customer.created || '').toLocaleDateString() : 
                            '-'
                          }
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-center">
                        {getPenaltyBadge(customer)}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">{getStatusBadge(customer.is_active)}</td>
                      <td className="px-6 py-4 text-center">
                        <Button
                          size="sm"
                          variant={customer.is_active ? "destructive" : "success"}
                          onClick={() => handleToggleActive(customer)}
                          disabled={updatingId === customer.id}
                        >
                          {updatingId === customer.id ? (
                            <>
                              <div className="animate-spin rounded-full h-3 w-3 border-b border-white mr-1"></div>
                            </>
                          ) : (
                            <>
                              {customer.is_active ? <UserX className="w-4 h-4 mr-1" /> : <UserCheck className="w-4 h-4 mr-1" />}
                              {customer.is_active ? "정지" : "복구"}
                            </>
                          )}
                        </Button>
                      </td>
                      <td className="px-6 py-4 text-center">
                        <Button
                          size="sm"
                          variant="outline"
                          onClick={() => handleViewDetail(customer.id)}
                          disabled={detailLoading}
                        >
                          상세
                        </Button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
              {customers.length === 0 && (
                <div className="text-center py-12 text-gray-500">데이터가 없습니다</div>
              )}
            </div>
          )}
        </div>

        {/* 더 불러오기 버튼 */}
        {hasMore && !loading && (
          <div className="flex justify-center mt-6">
            <Button variant="outline" onClick={loadMore} disabled={loadingMore}>
              {loadingMore ? "불러오는 중..." : "더 불러오기"}
            </Button>
          </div>
        )}

        {/* 상세조회 모달 */}
        <Dialog open={isDetailModalOpen} onOpenChange={setIsDetailModalOpen}>
          <DialogContent className="max-w-2xl">
            <DialogHeader>
              <DialogTitle>고객 상세 정보</DialogTitle>
            </DialogHeader>
            {selectedCustomer && (
              <div className="space-y-6">
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="text-sm font-medium text-gray-500">이름</label>
                    <p className="text-sm text-gray-900">{selectedCustomer.name}</p>
                  </div>
                  <div>
                    <label className="text-sm font-medium text-gray-500">이메일</label>
                    <p className="text-sm text-gray-900">{selectedCustomer.email}</p>
                  </div>
                  <div>
                    <label className="text-sm font-medium text-gray-500">전화번호</label>
                    <p className="text-sm text-gray-900">{selectedCustomer.phone || '-'}</p>
                  </div>
                  <div>
                    <label className="text-sm font-medium text-gray-500">생년월일</label>
                    <p className="text-sm text-gray-900">{selectedCustomer.birth || '-'}</p>
                  </div>
                  <div>
                    <label className="text-sm font-medium text-gray-500">성별</label>
                    <p className="text-sm text-gray-900">
                      {selectedCustomer.gender === 'M' ? '남성' : 
                       selectedCustomer.gender === 'F' ? '여성' : 
                       selectedCustomer.gender || '-'}
                    </p>
                  </div>
                  <div>
                    <label className="text-sm font-medium text-gray-500">가입 방법</label>
                    <p className="text-sm text-gray-900">
                      {selectedCustomer.signupMethod === 'USER' ? '일반' : 
                       selectedCustomer.signupMethod === 'KAKAO' ? '카카오' : 
                       selectedCustomer.signupMethod || '-'}
                    </p>
                  </div>
                  <div>
                    <label className="text-sm font-medium text-gray-500">이메일 인증</label>
                    <p className="text-sm text-gray-900">{selectedCustomer.isVerified ? '인증됨' : '미인증'}</p>
                  </div>
                  <div>
                    <label className="text-sm font-medium text-gray-500">계정 상태</label>
                    <p className="text-sm text-gray-900">{selectedCustomer.isActive ? '활성' : '비활성'}</p>
                  </div>
                  <div>
                    <label className="text-sm font-medium text-gray-500">패널티 점수</label>
                    <p className="text-sm text-gray-900">{selectedCustomer.penaltyScore || 0}점</p>
                  </div>
                  <div>
                    <label className="text-sm font-medium text-gray-500">가입일</label>
                    <p className="text-sm text-gray-900">{selectedCustomer.createdAt ? new Date(selectedCustomer.createdAt).toLocaleString() : '-'}</p>
                  </div>
                  <div>
                    <label className="text-sm font-medium text-gray-500">최종 수정일</label>
                    <p className="text-sm text-gray-900">{selectedCustomer.updatedAt ? new Date(selectedCustomer.updatedAt).toLocaleString() : '-'}</p>
                  </div>
                </div>
                <div className="col-span-2">
                  <label className="text-sm font-medium text-gray-500">주소</label>
                  <p className="text-sm text-gray-900">{selectedCustomer.address || '-'}</p>
                </div>
              </div>
            )}
          </DialogContent>
        </Dialog>
      </div>
    </div>
  );
};

export default Object.assign(CustomerListPage, { pageTitle: '고객 관리' });