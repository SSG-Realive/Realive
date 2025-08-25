'use client';
import React, { useEffect, useState } from 'react';
import { useRouter } from "next/navigation";
import apiClient from '@/lib/apiClient';
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Tabs, TabsList, TabsTrigger, TabsContent } from "@/components/ui/tabs";
import { Badge } from "@/components/ui/badge";

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
  createdAt?: string;
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
    apiClient.get(`/admin/users?userType=CUSTOMER&page=0&size=1000`, {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      }
    })
        .then(res => {
          setCustomers(res.data.data.content || []);
          const customerCount = res.data.data.totalElements || (res.data.data.content?.length ?? 0);
          // 판매자 목록도 불러와서 합산
          apiClient.get(`/admin/users?userType=SELLER&page=0&size=1000`, {
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
    apiClient.get('/admin/penalties?userType=CUSTOMER&page=0&size=100', {
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

  // 상태 뱃지 shadcn 적용
  const getStatusBadge = (user: any) => {
    const status = getStatusLabel(user);
    const isActive = status === '활성';
    return (
      <Badge 
        variant={isActive ? "success" : "destructive"}
        className={!isActive ? "text-white" : ""}
      >
        {status}
      </Badge>
    );
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 to-blue-50/50">
      <div className="p-8 max-w-7xl mx-auto">
        {/* 헤더 */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-2">회원 대시보드</h1>
          <p className="text-gray-600">고객과 판매자 정보를 한눈에 관리하세요</p>
        </div>

        {/* 통계 카드 */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
          <Card className="bg-white shadow-sm border-0 ring-1 ring-gray-100 hover:shadow-md transition-shadow">
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-gray-600 mb-1">전체 회원</p>
                  <p className="text-3xl font-bold text-gray-900">{totalMembers.toLocaleString()}</p>
                </div>
                <div className="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
                  <svg className="w-6 h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
                  </svg>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card className="bg-white shadow-sm border-0 ring-1 ring-gray-100 hover:shadow-md transition-shadow">
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-gray-600 mb-1">활성 회원</p>
                  <p className="text-3xl font-bold text-green-600">{totalActive.toLocaleString()}</p>
                </div>
                <div className="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center">
                  <svg className="w-6 h-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card className="bg-white shadow-sm border-0 ring-1 ring-gray-100 hover:shadow-md transition-shadow">
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-gray-600 mb-1">비활성 회원</p>
                  <p className="text-3xl font-bold text-red-600">{totalInactive.toLocaleString()}</p>
                </div>
                <div className="w-12 h-12 bg-red-100 rounded-lg flex items-center justify-center">
                  <svg className="w-6 h-6 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Tabs로 네비게이션 */}
        <Tabs defaultValue={tab.toLowerCase()} onValueChange={v => setTab(v.toUpperCase() as any)} className="mb-6">
          <TabsList>
            <TabsTrigger value="customer">고객 관리 <span className="ml-2 bg-gray-100 text-gray-900 py-0.5 px-2.5 rounded-full text-xs font-medium">{customers.length}</span></TabsTrigger>
            <TabsTrigger value="seller">판매자 관리 <span className="ml-2 bg-gray-100 text-gray-900 py-0.5 px-2.5 rounded-full text-xs font-medium">{sellers.length}</span></TabsTrigger>
          </TabsList>
          <TabsContent value="customer">
            <Card className="bg-white shadow-sm border-0 ring-1 ring-gray-100 mb-8">
              <CardHeader className="border-b border-gray-100">
                <CardTitle className="text-lg font-semibold text-gray-900">고객 목록</CardTitle>
              </CardHeader>
              <CardContent className="p-0">
                {loading ? (
                  <div className="flex items-center justify-center py-12">
                    <div className="flex items-center space-x-2 text-gray-500">
                      <div className="w-4 h-4 border-2 border-gray-300 border-t-blue-500 rounded-full animate-spin"></div>
                      <span>로딩 중...</span>
                    </div>
                  </div>
                ) : (
                  <ScrollArea className="h-[500px]">
                    <table className="min-w-full divide-y divide-gray-200">
                      <thead className="bg-gray-50 sticky top-0">
                        <tr>
                          <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">이름</th>
                          <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">이메일</th>
                          <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">상태</th>
                        </tr>
                      </thead>
                      <tbody className="bg-white divide-y divide-gray-200">
                        {customers.map(user => (
                          <tr key={user.id} className="hover:bg-gray-50 transition-colors">
                            <td className="px-6 py-4 whitespace-nowrap">
                              <div className="flex items-center">
                                <div className="flex-shrink-0 h-10 w-10">
                                  <div className="h-10 w-10 rounded-full bg-gray-200 flex items-center justify-center">
                                    <span className="text-sm font-medium text-gray-700">{user.name?.charAt(0) || '?'}</span>
                                  </div>
                                </div>
                                <div className="ml-4">
                                  <div className="text-sm font-medium text-gray-900">{user.name}</div>
                                </div>
                              </div>
                            </td>
                            <td className="px-6 py-4 whitespace-nowrap">
                              <div className="text-sm text-gray-900">{user.email}</div>
                            </td>
                            <td className="px-6 py-4 whitespace-nowrap">{getStatusBadge(user)}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                    {customers.length === 0 && (
                      <div className="text-center py-12 text-gray-500">데이터가 없습니다</div>
                    )}
                  </ScrollArea>
                )}
              </CardContent>
            </Card>
          </TabsContent>
          <TabsContent value="seller">
            <Card className="bg-white shadow-sm border-0 ring-1 ring-gray-100 mb-8">
              <CardHeader className="border-b border-gray-100">
                <CardTitle className="text-lg font-semibold text-gray-900">판매자 목록</CardTitle>
              </CardHeader>
              <CardContent className="p-0">
                {loading ? (
                  <div className="flex items-center justify-center py-12">
                    <div className="flex items-center space-x-2 text-gray-500">
                      <div className="w-4 h-4 border-2 border-gray-300 border-t-blue-500 rounded-full animate-spin"></div>
                      <span>로딩 중...</span>
                    </div>
                  </div>
                ) : (
                  <ScrollArea className="h-[500px]">
                    <table className="min-w-full divide-y divide-gray-200">
                      <thead className="bg-gray-50 sticky top-0">
                        <tr>
                          <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">이름</th>
                          <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">이메일</th>
                          <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">사업자번호</th>
                          <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">가입일</th>
                          <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">상태</th>
                        </tr>
                      </thead>
                      <tbody className="bg-white divide-y divide-gray-200">
                        {sellers.map(user => (
                          <tr key={user.id} className="hover:bg-gray-50 transition-colors">
                            <td className="px-6 py-4 whitespace-nowrap">
                              <div className="flex items-center">
                                <div className="flex-shrink-0 h-10 w-10">
                                  <div className="h-10 w-10 rounded-full bg-gray-200 flex items-center justify-center">
                                    <span className="text-sm font-medium text-gray-700">{user.name?.charAt(0) || '?'}</span>
                                  </div>
                                </div>
                                <div className="ml-4">
                                  <div className="text-sm font-medium text-gray-900">{user.name}</div>
                                </div>
                              </div>
                            </td>
                            <td className="px-6 py-4 whitespace-nowrap">
                              <div className="text-sm text-gray-900">{user.email}</div>
                            </td>
                            <td className="px-6 py-4 whitespace-nowrap">
                              <div className="text-sm text-gray-900">{user.businessNumber || '-'}</div>
                            </td>
                            <td className="px-6 py-4 whitespace-nowrap">
                              <div className="text-sm text-gray-900">{user.createdAt ? new Date(user.createdAt).toLocaleDateString() : '-'}</div>
                            </td>
                            <td className="px-6 py-4 whitespace-nowrap">{getStatusBadge(user)}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                    {sellers.length === 0 && (
                      <div className="text-center py-12 text-gray-500">데이터가 없습니다</div>
                    )}
                  </ScrollArea>
                )}
              </CardContent>
            </Card>
          </TabsContent>
        </Tabs>

        {/* 패널티 목록도 동일하게 ScrollArea, Badge 적용 */}
        <Card className="bg-white shadow-sm border-0 ring-1 ring-gray-100">
          <CardHeader className="border-b border-gray-100">
            <CardTitle className="text-lg font-semibold text-gray-900">
              사용자 패널티
              <span className="ml-2 bg-red-100 text-red-800 py-0.5 px-2.5 rounded-full text-xs font-medium">{penalties.length}</span>
            </CardTitle>
          </CardHeader>
          <CardContent className="p-0">
            <ScrollArea className="h-[300px]">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50 sticky top-0">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">사용자</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">패널티 사유</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">발생일</th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {penalties.length === 0 ? (
                    <tr>
                      <td colSpan={3} className="px-6 py-12 text-center text-gray-500">패널티 내역이 없습니다</td>
                    </tr>
                  ) : (
                    penalties.map((penalty, idx) => (
                      <tr key={penalty.id || idx} className="hover:bg-gray-50 transition-colors">
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div className="text-sm font-medium text-gray-900">{getPenaltyUserName(penalty)}</div>
                        </td>
                        <td className="px-6 py-4">
                          <div className="text-sm text-gray-900">{penalty.reason || penalty.cause || penalty.description || '-'}</div>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div className="text-sm text-gray-900">{penalty.date || penalty.createdAt ? new Date(penalty.date || penalty.createdAt!).toLocaleDateString() : '-'}</div>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </ScrollArea>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}