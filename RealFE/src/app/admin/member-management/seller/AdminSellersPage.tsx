'use client';
import React, { useState, useEffect, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import { adminApi } from '@/lib/apiClient';
import { Users, Search, UserCheck, UserX, CheckCircle, XCircle} from 'lucide-react';
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from "@/components/ui/dialog";

interface Seller {
  id: number;
  name: string;
  email: string;
  image?: string;
  is_approved: boolean;
  is_active: boolean;
  businessNumber?: string;
  createdAt?: string;
  created?: string;
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

interface SellerDetail {
  id: number;
  name: string;
  email: string;
  phone: string;
  businessNumber: string;
  isApproved: boolean;
  approvedAt: string;
  isActive: boolean;
  createdAt: string;
  created?: string;
  updatedAt: string;
}

const AdminSellersPage: React.FC = () => {
  const [sellers, setSellers] = useState<Seller[]>([]);
  const [search, setSearch] = useState("");
  const [status, setStatus] = useState("all");
  const [activeFilter, setActiveFilter] = useState("all");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [updatingId, setUpdatingId] = useState<number | null>(null);
  const [selectedSeller, setSelectedSeller] = useState<SellerDetail | null>(null);
  const [isDetailModalOpen, setIsDetailModalOpen] = useState(false);
  const [detailLoading, setDetailLoading] = useState(false);
  const [showSuccessModal, setShowSuccessModal] = useState(false);
  const [showErrorModal, setShowErrorModal] = useState(false);
  const [modalMessage, setModalMessage] = useState('');
  const [totalStats, setTotalStats] = useState({
    total: 0,
    active: 0,
    inactive: 0,
    approved: 0,
    pending: 0
  });
  const router = useRouter();

  // 인증 체크
  useEffect(() => {
    if (typeof window !== 'undefined' && !localStorage.getItem('adminToken')) {
      router.replace('/admin/login');
      return;
    }
  }, [router]);

  // 판매자 통계 데이터 로딩
  const fetchTotalStats = async () => {
    try {
      const response = await adminApi.get('/admin/users?userType=SELLER&page=0&size=1000');
      const sellerData = response.data.data.content || [];
      
      const totalActive = sellerData.filter((seller: any) => 
        seller.is_active === true || seller.is_active === 'true' || seller.is_active === 1 ||
        seller.isActive === true || seller.isActive === 'true' || seller.isActive === 1
      ).length;
      
      const totalApproved = sellerData.filter((seller: any) => 
        seller.is_approved === true || seller.is_approved === 'true' || seller.is_approved === 1 ||
        seller.isApproved === true || seller.isApproved === 'true' || seller.isApproved === 1
      ).length;
      
      setTotalStats({
        total: sellerData.length,
        active: totalActive,
        inactive: sellerData.length - totalActive,
        approved: totalApproved,
        pending: sellerData.length - totalApproved
      });
    } catch (err) {
      console.error('판매자 통계 로딩 실패:', err);
    }
  };

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
    fetchTotalStats();
  }, [fetchSellers]);

  // 필터링된 판매자 목록
  const filteredSellers = sellers.filter(seller => {
    const matchesSearch = search === "" || 
      seller.name.toLowerCase().includes(search.toLowerCase()) ||
      seller.email.toLowerCase().includes(search.toLowerCase());
    
    const matchesStatus = status === "all" || 
      (status === "승인" && seller.is_approved) ||
      (status === "승인처리전" && !seller.is_approved);
    
    const matchesActiveFilter = activeFilter === "all" ||
      (activeFilter === "active" && seller.is_active) ||
      (activeFilter === "inactive" && !seller.is_active);
    
    return matchesSearch && matchesStatus && matchesActiveFilter;
  });

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
      
      // 상태 변경 후 전체 통계 다시 로딩
      fetchTotalStats();
    } catch (err) {
      const error = err as ErrorResponse;
      alert(`상태 변경 실패: ${error?.response?.data?.message || error?.message || '알 수 없는 오류'}`);
    } finally {
      setUpdatingId(null);
    }
  }, [fetchTotalStats]);

  // 상세조회 핸들러
  const handleViewDetail = async (sellerId: number) => {
    try {
      setDetailLoading(true);
      const response = await adminApi.get(`/admin/users/sellers/${sellerId}`);
      setSelectedSeller(response.data.data);
      setIsDetailModalOpen(true);
    } catch (err) {
      const error = err as ErrorResponse;
      alert(`상세 정보 조회 실패: ${error?.response?.data?.message || error?.message || '알 수 없는 오류'}`);
    } finally {
      setDetailLoading(false);
    }
  };

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
      
      // 승인/거절 후 전체 통계 다시 로딩
      fetchTotalStats();
      
      setModalMessage(`${seller.name} ${approved ? '승인' : '거절'} 처리되었습니다.`);
      setShowSuccessModal(true);
    } catch (err) {
      const error = err as ErrorResponse;
      setModalMessage(`처리 실패: ${error?.response?.data?.message || error?.message || '알 수 없는 오류'}`);
      setShowErrorModal(true);
    } finally {
      setUpdatingId(null);
    }
  }, [fetchTotalStats]);

  // 이벤트 핸들러
  const handleSearchChange = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    setSearch(e.target.value);
  }, []);
  useCallback((e: React.ChangeEvent<HTMLSelectElement>) => {
    setStatus(e.target.value);
  }, []);
  useCallback((e: React.ChangeEvent<HTMLSelectElement>) => {
    setActiveFilter(e.target.value);
  }, []);
  if (typeof window !== 'undefined' && !localStorage.getItem('adminToken')) {
    return null;
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 to-slate-100">
      <div className="max-w-7xl mx-auto p-4 sm:p-6 lg:p-8">
        {/* 헤더 */}
        <div className="mb-8">
          <div className="flex items-center gap-3 mb-4">
            <div className="p-3 bg-purple-600 rounded-xl">
              <Users className="w-8 h-8 text-white" />
            </div>
            <div>
              <h1 className="text-3xl font-bold text-gray-900">판매자 관리</h1>
              <p className="text-gray-600 mt-1">판매자 계정을 관리하고 승인/거절 및 상태를 변경할 수 있습니다</p>
            </div>
          </div>

          {/* 통계 카드 */}
          <div className="grid grid-cols-1 md:grid-cols-5 gap-6 mb-8">
            <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
              <div className="flex items-center">
                <div className="flex-shrink-0">
                  <div className="w-8 h-8 bg-purple-100 rounded-lg flex items-center justify-center">
                    <Users className="w-5 h-5 text-purple-600" />
                  </div>
                </div>
                <div className="ml-4">
                  <p className="text-sm font-medium text-gray-600">전체 판매자</p>
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
                  <p className="text-sm font-medium text-gray-600">활성 판매자</p>
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
                  <p className="text-sm font-medium text-gray-600">정지 판매자</p>
                  <p className="text-2xl font-bold text-red-600">{totalStats.inactive.toLocaleString()}</p>
                </div>
              </div>
            </div>

            <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
              <div className="flex items-center">
                <div className="flex-shrink-0">
                  <div className="w-8 h-8 bg-blue-100 rounded-lg flex items-center justify-center">
                    <CheckCircle className="w-5 h-5 text-blue-600" />
                  </div>
                </div>
                <div className="ml-4">
                  <p className="text-sm font-medium text-gray-600">승인 판매자</p>
                  <p className="text-2xl font-bold text-blue-600">{totalStats.approved.toLocaleString()}</p>
                </div>
              </div>
            </div>

            <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
              <div className="flex items-center">
                <div className="flex-shrink-0">
                  <div className="w-8 h-8 bg-yellow-100 rounded-lg flex items-center justify-center">
                    <XCircle className="w-5 h-5 text-yellow-600" />
                  </div>
                </div>
                <div className="ml-4">
                  <p className="text-sm font-medium text-gray-600">승인 대기</p>
                  <p className="text-2xl font-bold text-yellow-600">{totalStats.pending.toLocaleString()}</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* 필터 및 검색 */}
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 mb-8">
          <div className="grid grid-cols-1 md:grid-cols-5 gap-4 items-end">
            <div className="md:col-span-2">
              <label className="block text-sm font-medium text-gray-700 mb-2">검색</label>
              <div className="relative">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
                <Input
                  type="text"
                  placeholder="이름 또는 이메일로 검색"
                  value={search}
                  onChange={handleSearchChange}
                  className="w-full pl-10"
                />
              </div>
            </div>
            
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">승인 상태</label>
                <Select value={status} onValueChange={(value) => setStatus(value)}>
                <SelectTrigger className="w-full">
                  <SelectValue placeholder="승인 상태 선택" />
                  </SelectTrigger>
                  <SelectContent>
                  <SelectItem value="all">모든 상태</SelectItem>
                    <SelectItem value="승인">승인</SelectItem>
                    <SelectItem value="승인처리전">승인처리전</SelectItem>
                  </SelectContent>
                </Select>
            </div>
            
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">활성 상태</label>
                <Select value={activeFilter} onValueChange={(value) => setActiveFilter(value)}>
                <SelectTrigger className="w-full">
                  <SelectValue placeholder="활성 상태 선택" />
                  </SelectTrigger>
                  <SelectContent>
                  <SelectItem value="all">모든 상태</SelectItem>
                    <SelectItem value="active">활성</SelectItem>
                    <SelectItem value="inactive">정지</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            
            <div>
              <Button 
                variant="outline" 
                onClick={() => {
                  fetchSellers();
                  fetchTotalStats();
                }} 
                disabled={loading}
                size="sm"
                className="w-full"
              >
                새로고침
              </Button>
            </div>
          </div>
        </div>

        {/* 판매자 목록 */}
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
          {loading && filteredSellers.length === 0 ? (
            <div className="flex items-center justify-center py-16">
              <div className="text-center">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-purple-600 mx-auto mb-4"></div>
                <p className="text-gray-600">판매자 정보를 불러오는 중...</p>
              </div>
            </div>
          ) : error ? (
            <div className="flex items-center justify-center py-16">
              <div className="text-center">
                <div className="text-red-500 text-lg font-semibold mb-2">오류가 발생했습니다</div>
                <div className="text-gray-600 mb-6">{error}</div>
                <Button
                  onClick={fetchSellers}
                  variant="default"
                >
                  다시 시도
                </Button>
              </div>
            </div>
          ) : filteredSellers.length === 0 ? (
            <div className="flex items-center justify-center py-16">
              <div className="text-center">
                <Users className="w-16 h-16 text-gray-300 mx-auto mb-4" />
                <div className="text-gray-500 text-lg font-medium mb-2">판매자가 없습니다</div>
                <div className="text-gray-400 text-sm">
                  {search || status || activeFilter ? '검색 조건을 변경해보세요.' : '등록된 판매자가 없습니다.'}
                </div>
              </div>
            </div>
          ) : (
            <>
              {/* 데스크탑 테이블 */}
              <div className="hidden lg:block overflow-x-auto">
                <table className="min-w-full">
                  <thead className="bg-gray-50">
                    <tr>
                      <th className="px-6 py-4 text-left text-sm font-semibold text-gray-900">판매자 정보</th>
                      <th className="px-6 py-4 text-left text-sm font-semibold text-gray-900">이메일</th>
                      <th className="px-6 py-4 text-left text-sm font-semibold text-gray-900">사업자번호</th>
                      <th className="px-6 py-4 text-left text-sm font-semibold text-gray-900">가입일</th>
                      <th className="px-6 py-4 text-center text-sm font-semibold text-gray-900">승인 상태</th>
                      <th className="px-6 py-4 text-center text-sm font-semibold text-gray-900">활성 상태</th>
                      <th className="px-6 py-4 text-center text-sm font-semibold text-gray-900">정지</th>
                      <th className="px-6 py-4 text-center text-sm font-semibold text-gray-900">상세</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-100">
                    {filteredSellers.map((seller) => (
                      <tr key={seller.id} className="hover:bg-gray-50 transition-colors">
                        <td className="px-6 py-4">
                          <div className="flex items-center gap-4">
                            <img
                              src={seller.image || '/images/default-profile.svg'}
                              alt={`${seller.name}의 프로필`}
                              className="h-12 w-12 rounded-full object-cover border-2 border-gray-200"
                              onError={(e) => {
                                (e.currentTarget as HTMLImageElement).src = '/images/placeholder.png';
                              }}
                            />
                            <div>
                              <div className="font-semibold text-gray-900">{seller.name}</div>
                              <div className="text-sm text-gray-500">ID: {seller.id}</div>
                            </div>
                          </div>
                        </td>
                        <td className="px-6 py-4">
                          <div className="text-gray-900">{seller.email}</div>
                        </td>
                        <td className="px-6 py-4">
                          <div className="text-gray-900">{seller.businessNumber || '-'}</div>
                        </td>
                        <td className="px-6 py-4">
                          <div className="text-gray-900">
                            {(seller.createdAt || seller.created) ? 
                              new Date(seller.createdAt || seller.created || '').toLocaleDateString() : 
                              '-'
                            }
                          </div>
                        </td>
                        <td className="px-6 py-4 text-center">
                          <span className={`inline-flex items-center px-3 py-1 text-sm font-medium rounded-full ${
                            seller.is_approved
                              ? 'bg-blue-100 text-blue-800'
                              : 'bg-yellow-100 text-yellow-800'
                          }`}>
                            {seller.is_approved ? '승인' : '승인처리전'}
                          </span>
                        </td>
                        <td className="px-6 py-4 text-center">
                          <span className={`inline-flex items-center px-3 py-1 text-sm font-medium rounded-full ${
                            seller.is_active
                              ? 'bg-green-100 text-green-800'
                              : 'bg-red-100 text-red-800'
                          }`}>
                            {seller.is_active ? '활성' : '정지'}
                          </span>
                        </td>
                        <td className="px-6 py-4 text-center">
                          <div className="flex flex-col gap-2">
                            {!seller.is_approved && (
                              <div className="flex gap-1 justify-center">
                                <Button
                                  onClick={() => handleApproval(seller, true)}
                                  disabled={updatingId === seller.id}
                                  variant="default"
                                  size="sm"
                                >
                                  승인
                                </Button>
                                <Button
                                  onClick={() => handleApproval(seller, false)}
                                  disabled={updatingId === seller.id}
                                  variant="outline"
                                  size="sm"
                                >
                                  거절
                                </Button>
                              </div>
                            )}
                            <Button
                              onClick={() => handleToggleActive(seller)}
                              disabled={updatingId === seller.id}
                              variant={seller.is_active ? "destructive" : "success"}
                              size="sm"
                            >
                              {updatingId === seller.id ? (
                                <>
                                  <div className="animate-spin rounded-full h-3 w-3 border-b border-white mr-1"></div>
                                </>
                              ) : (
                                  <>
                                    {seller.is_active ? <UserX className="w-4 h-4 mr-1" /> : <UserCheck className="w-4 h-4 mr-1" />}
                                    {seller.is_active ? "정지" : "복구"}
                                  </>
                              )}
                            </Button>
                          </div>
                        </td>
                        <td className="px-6 py-4 text-center">
                          <Button
                            onClick={() => handleViewDetail(seller.id)}
                            disabled={detailLoading}
                            variant="outline"
                            size="sm"
                          >
                            상세
                          </Button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              {/* 모바일 카드 */}
              <div className="lg:hidden divide-y divide-gray-100">
                {filteredSellers.map((seller) => (
                  <div key={seller.id} className="p-6">
                    <div className="flex items-start gap-4">
                      <img
                        src={seller.image || '/images/default-profile.svg'}
                        alt={`${seller.name}의 프로필`}
                        className="h-12 w-12 rounded-full object-cover border-2 border-gray-200 flex-shrink-0"
                        onError={(e) => {
                          (e.currentTarget as HTMLImageElement).src = '/images/placeholder.png';
                        }}
                      />
                      <div className="flex-1 min-w-0">
                        <div className="flex items-start justify-between mb-2">
                          <div>
                            <h3 className="font-semibold text-gray-900 truncate">{seller.name}</h3>
                            <p className="text-sm text-gray-500">ID: {seller.id}</p>
                          </div>
                          <div className="flex flex-col gap-1">
                            <span className={`inline-flex items-center px-2 py-1 text-xs font-medium rounded-full ${
                              seller.is_approved
                                ? 'bg-blue-100 text-blue-800'
                                : 'bg-yellow-100 text-yellow-800'
                            }`}>
                              {seller.is_approved ? '승인' : '승인처리전'}
                            </span>
                            <span className={`inline-flex items-center px-2 py-1 text-xs font-medium rounded-full ${
                              seller.is_active
                                ? 'bg-green-100 text-green-800'
                                : 'bg-red-100 text-red-800'
                            }`}>
                              {seller.is_active ? '활성' : '정지'}
                            </span>
                          </div>
                        </div>
                        <p className="text-sm text-gray-600 mb-2 truncate">{seller.email}</p>
                        <div className="text-sm text-gray-500 mb-3 space-y-1">
                          <div>사업자번호: {seller.businessNumber || '-'}</div>
                          <div>가입일: {(seller.createdAt || seller.created) ? new Date(seller.createdAt || seller.created || '').toLocaleDateString() : '-'}</div>
                        </div>
                        <div className="flex flex-wrap gap-2">
                          <Button
                            onClick={() => handleViewDetail(seller.id)}
                            disabled={detailLoading}
                            variant="outline"
                            size="sm"
                          >
                            상세
                          </Button>
                          {!seller.is_approved && (
                            <>
                              <Button
                                onClick={() => handleApproval(seller, true)}
                                disabled={updatingId === seller.id}
                                variant="default"
                                size="sm"
                              >
                                승인
                              </Button>
                              <Button
                                onClick={() => handleApproval(seller, false)}
                                disabled={updatingId === seller.id}
                                variant="outline"
                                size="sm"
                              >
                                거절
                              </Button>
                            </>
                          )}
                          <Button
                            onClick={() => handleToggleActive(seller)}
                            disabled={updatingId === seller.id}
                            variant={seller.is_active ? "destructive" : "success"}
                            size="sm"
                          >
                            {updatingId === seller.id ? (
                              <>
                                <div className="animate-spin rounded-full h-3 w-3 border-b border-white mr-1"></div>
                                처리중
                              </>
                            ) : (
                                <>
                                  {seller.is_active ? <UserX className="w-4 h-4 mr-1" /> : <UserCheck className="w-4 h-4 mr-1" />}
                                  {seller.is_active ? "정지" : "복구"}
                                </>
                            )}
                          </Button>
                        </div>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </>
          )}
        </div>

        {/* 상세조회 모달 */}
        <Dialog open={isDetailModalOpen} onOpenChange={setIsDetailModalOpen}>
          <DialogContent className="max-w-2xl">
            <DialogHeader>
              <DialogTitle>판매자 상세 정보</DialogTitle>
            </DialogHeader>
            {selectedSeller && (
              <div className="space-y-6">
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="text-sm font-medium text-gray-500">이름</label>
                    <p className="text-sm text-gray-900">{selectedSeller.name}</p>
                  </div>
                  <div>
                    <label className="text-sm font-medium text-gray-500">이메일</label>
                    <p className="text-sm text-gray-900">{selectedSeller.email}</p>
                  </div>
                  <div>
                    <label className="text-sm font-medium text-gray-500">전화번호</label>
                    <p className="text-sm text-gray-900">{selectedSeller.phone || '-'}</p>
                  </div>
                  <div>
                    <label className="text-sm font-medium text-gray-500">사업자번호</label>
                    <p className="text-sm text-gray-900">{selectedSeller.businessNumber || '-'}</p>
                  </div>
                  <div>
                    <label className="text-sm font-medium text-gray-500">승인 상태</label>
                    <p className="text-sm text-gray-900">{selectedSeller.isApproved ? '승인됨' : '승인 대기'}</p>
                  </div>
                  <div>
                    <label className="text-sm font-medium text-gray-500">계정 상태</label>
                    <p className="text-sm text-gray-900">{selectedSeller.isActive ? '활성' : '비활성'}</p>
                  </div>
                  <div>
                    <label className="text-sm font-medium text-gray-500">승인일</label>
                    <p className="text-sm text-gray-900">{selectedSeller.approvedAt ? new Date(selectedSeller.approvedAt).toLocaleString() : '-'}</p>
                  </div>
                  <div>
                    <label className="text-sm font-medium text-gray-500">가입일</label>
                    <p className="text-sm text-gray-900">
                      {(selectedSeller.createdAt || selectedSeller.created) ? 
                        new Date(selectedSeller.createdAt || selectedSeller.created || '').toLocaleString() : 
                        '-'
                      }
                    </p>
                  </div>
                  <div>
                    <label className="text-sm font-medium text-gray-500">최종 수정일</label>
                    <p className="text-sm text-gray-900">{selectedSeller.updatedAt ? new Date(selectedSeller.updatedAt).toLocaleString() : '-'}</p>
                  </div>
                </div>
              </div>
            )}
          </DialogContent>
        </Dialog>

        {/* 성공 모달 */}
        <Dialog open={showSuccessModal} onOpenChange={setShowSuccessModal}>
          <DialogContent className="max-w-md">
            <DialogHeader>
              <DialogTitle className="flex items-center gap-2">
                <CheckCircle className="w-5 h-5 text-green-600" />
                처리 완료
              </DialogTitle>
            </DialogHeader>
            <div className="py-4">
              <p className="text-gray-700">{modalMessage}</p>
            </div>
            <DialogFooter>
              <Button onClick={() => setShowSuccessModal(false)} variant="default">
                확인
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>

        {/* 에러 모달 */}
        <Dialog open={showErrorModal} onOpenChange={setShowErrorModal}>
          <DialogContent className="max-w-md">
            <DialogHeader>
              <DialogTitle className="flex items-center gap-2">
                <XCircle className="w-5 h-5 text-red-600" />
                처리 실패
              </DialogTitle>
            </DialogHeader>
            <div className="py-4">
              <p className="text-red-600">{modalMessage}</p>
            </div>
            <DialogFooter>
              <Button onClick={() => setShowErrorModal(false)} variant="outline">
                확인
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      </div>
    </div>
  );
};

export default Object.assign(AdminSellersPage, { pageTitle: '판매자 관리' }); 