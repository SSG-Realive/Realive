"use client";
import React, { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import apiClient from '@/lib/apiClient';
import { AlertTriangle, Search, Plus, Eye, Trash2 } from 'lucide-react';
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";

interface Penalty {
  id: number;
  customerId: number;
  reason: string;
  createdAt: string;
  penaltyScore?: number; // 패널티 점수 추가
}

export default function PenaltyListPage() {
  const router = useRouter();
  const [penalties, setPenalties] = useState<Penalty[]>([]);
  const [customers, setCustomers] = useState<{id: number, name: string, email: string}[]>([]);
  const [search, setSearch] = useState("");
  const [loading, setLoading] = useState(true);
  const [deletingId, setDeletingId] = useState<number | null>(null);

  useEffect(() => {
    const token = typeof window !== 'undefined' ? localStorage.getItem('adminToken') : '';
    
    // 패널티 목록 로딩
    apiClient.get('/admin/penalties?userType=CUSTOMER&page=0&size=100', {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      }
    })
      .then(res => {
        setPenalties(res.data.content || []);
        setLoading(false);
      })
      .catch((error) => {
        console.error('패널티 목록 로딩 실패:', error);
        setPenalties([]);
        setLoading(false);
      });
    
    // 고객 목록 로딩
    apiClient.get('/admin/users?userType=CUSTOMER&page=0&size=100', {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      }
    })
      .then(res => setCustomers(res.data.data.content || []))
      .catch(() => setCustomers([]));
  }, []);

  // 패널티 삭제 핸들러
  const handleDeletePenalty = async (penaltyId: number, customerId: number) => {
    if (!confirm('이 패널티를 삭제하시겠습니까? 삭제 시 해당 고객의 패널티 점수도 차감됩니다.')) {
      return;
    }

    try {
      setDeletingId(penaltyId);
      const token = localStorage.getItem('adminToken');
      
      await apiClient.delete(`/admin/penalties/${penaltyId}`, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        }
      });

      // 삭제 성공 시 목록에서 제거
      setPenalties(prev => prev.filter(p => p.id !== penaltyId));
      alert('패널티가 성공적으로 삭제되었습니다.');
    } catch (error: any) {
      console.error('패널티 삭제 실패:', error);
      alert(`패널티 삭제 실패: ${error?.response?.data?.message || error?.message || '알 수 없는 오류'}`);
    } finally {
      setDeletingId(null);
    }
  };

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
    <div className="min-h-screen bg-gradient-to-br from-slate-50 to-slate-100">
      <div className="max-w-7xl mx-auto p-4 sm:p-6 lg:p-8">
        {/* 헤더 */}
        <div className="mb-8">
          <div className="flex items-center gap-3 mb-4">
            <div className="p-3 bg-red-600 rounded-xl">
              <AlertTriangle className="w-8 h-8 text-white" />
            </div>
            <div>
              <h1 className="text-3xl font-bold text-gray-900">사용자 패널티 관리</h1>
              <p className="text-gray-600 mt-1">사용자 패널티를 조회하고 관리할 수 있습니다</p>
            </div>
          </div>

          {/* 통계 카드 */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
            <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
              <div className="flex items-center">
                <div className="flex-shrink-0">
                  <div className="w-8 h-8 bg-red-100 rounded-lg flex items-center justify-center">
                    <AlertTriangle className="w-5 h-5 text-red-600" />
                  </div>
                </div>
                <div className="ml-4">
                  <p className="text-sm font-medium text-gray-600">전체 패널티</p>
                  <p className="text-2xl font-bold text-gray-900">{penalties.length.toLocaleString()}</p>
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
                  <p className="text-sm font-medium text-gray-600">이번 달 패널티</p>
                  <p className="text-2xl font-bold text-orange-600">
                    {penalties.filter(p => {
                      const penaltyDate = new Date(p.createdAt);
                      const now = new Date();
                      return penaltyDate.getMonth() === now.getMonth() && penaltyDate.getFullYear() === now.getFullYear();
                    }).length.toLocaleString()}
                  </p>
                </div>
              </div>
            </div>
            
            <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
              <div className="flex items-center">
                <div className="flex-shrink-0">
                  <div className="w-8 h-8 bg-blue-100 rounded-lg flex items-center justify-center">
                    <Eye className="w-5 h-5 text-blue-600" />
                  </div>
                </div>
                <div className="ml-4">
                  <p className="text-sm font-medium text-gray-600">검색 결과</p>
                  <p className="text-2xl font-bold text-blue-600">{filtered.length.toLocaleString()}</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* 검색 및 액션 */}
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 mb-8">
          <div className="flex flex-col lg:flex-row gap-4">
            <div className="flex-1">
              <div className="relative">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
                <Input
                  type="text"
                  placeholder="사용자 ID 또는 사유로 검색하세요..."
                  value={search}
                  onChange={e => setSearch(e.target.value)}
                  className="w-full pl-10"
                />
              </div>
            </div>
            
            <Button
              className="lg:w-auto w-full"
              variant="destructive"
              onClick={() => router.push('/admin/member-management/penalty/register')}
            >
              <Plus className="w-5 h-5" />
              패널티 등록
            </Button>
          </div>
        </div>

        {/* 패널티 목록 */}
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
          {loading ? (
            <div className="flex items-center justify-center py-16">
              <div className="text-center">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-red-600 mx-auto mb-4"></div>
                <p className="text-gray-600">패널티 정보를 불러오는 중...</p>
              </div>
            </div>
          ) : filtered.length === 0 ? (
            <div className="flex items-center justify-center py-16">
              <div className="text-center">
                <AlertTriangle className="w-16 h-16 text-gray-300 mx-auto mb-4" />
                <div className="text-gray-500 text-lg font-medium mb-2">패널티가 없습니다</div>
                <div className="text-gray-400 text-sm">
                  {search ? '검색 조건을 변경해보세요.' : '등록된 패널티가 없습니다.'}
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
                      <th className="px-6 py-4 text-left text-sm font-semibold text-gray-900">번호</th>
                      <th className="px-6 py-4 text-left text-sm font-semibold text-gray-900">사용자</th>
                      <th className="px-6 py-4 text-left text-sm font-semibold text-gray-900">패널티 사유</th>
                      <th className="px-6 py-4 text-left text-sm font-semibold text-gray-900">발생일</th>
                      <th className="px-6 py-4 text-center text-sm font-semibold text-gray-900">액션</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-100">
                    {filtered.map((penalty, idx) => (
                      <tr key={penalty.id} className="hover:bg-gray-50 transition-colors">
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div className="text-sm font-medium text-gray-900">{idx + 1}</div>
                        </td>
                        <td className="px-6 py-4">
                          <div className="text-sm text-gray-900">{getCustomerName(penalty.customerId)}</div>
                        </td>
                        <td className="px-6 py-4">
                          <div className="text-sm text-gray-900 max-w-md truncate" title={penalty.reason}>
                            {penalty.reason}
                          </div>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div className="text-sm text-gray-900">
                            {penalty.createdAt ? (() => {
                              try {
                                const date = new Date(penalty.createdAt);
                                return isNaN(date.getTime()) ? penalty.createdAt : date.toLocaleDateString();
                              } catch (error) {
                                return penalty.createdAt;
                              }
                            })() : '-'}
                          </div>
                        </td>
                        <td className="px-6 py-4 text-center">
                          <div className="flex gap-2 justify-center">
                            <Button
                              onClick={() => router.push(`/admin/member-management/penalty/${penalty.id}`)}
                              variant="link"
                              size="sm"
                            >
                              <Eye className="w-4 h-4 mr-1" />
                              상세보기
                            </Button>
                            <Button
                              onClick={() => handleDeletePenalty(penalty.id, penalty.customerId)}
                              variant="destructive"
                              size="sm"
                              disabled={deletingId === penalty.id}
                            >
                              {deletingId === penalty.id ? (
                                <>
                                  <div className="animate-spin rounded-full h-3 w-3 border-b border-white mr-1"></div>
                                  삭제중
                                </>
                              ) : (
                                <>
                                  <Trash2 className="w-4 h-4 mr-1" />
                                  삭제
                                </>
                              )}
                            </Button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              {/* 모바일 카드 */}
              <div className="lg:hidden divide-y divide-gray-100">
                {filtered.map((penalty, idx) => (
                  <div key={penalty.id} className="p-6">
                    <div className="flex items-start justify-between mb-2">
                      <div className="flex-1">
                        <h3 className="font-semibold text-gray-900">#{idx + 1}</h3>
                        <p className="text-sm text-gray-500 mb-2">{getCustomerName(penalty.customerId)}</p>
                        <p className="text-sm text-gray-600 mb-2">{penalty.reason}</p>
                        <p className="text-xs text-gray-400">
                          {penalty.createdAt ? (() => {
                            try {
                              const date = new Date(penalty.createdAt);
                              return isNaN(date.getTime()) ? penalty.createdAt : date.toLocaleDateString();
                            } catch (error) {
                              return penalty.createdAt;
                            }
                          })() : '-'}
                        </p>
                      </div>
                      <div className="flex flex-col gap-2">
                        <Button
                          onClick={() => router.push(`/admin/member-management/penalty/${penalty.id}`)}
                          variant="link"
                          size="sm"
                        >
                          <Eye className="w-4 h-4 mr-1" />
                          상세보기
                        </Button>
                        <Button
                          onClick={() => handleDeletePenalty(penalty.id, penalty.customerId)}
                          variant="destructive"
                          size="sm"
                          disabled={deletingId === penalty.id}
                        >
                          {deletingId === penalty.id ? (
                            <>
                              <div className="animate-spin rounded-full h-3 w-3 border-b border-white mr-1"></div>
                              삭제중
                            </>
                          ) : (
                            <>
                              <Trash2 className="w-4 h-4 mr-1" />
                              삭제
                            </>
                          )}
                        </Button>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
}