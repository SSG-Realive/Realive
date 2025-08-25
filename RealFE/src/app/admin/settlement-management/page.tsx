"use client";
import React, { useState, useEffect, useCallback } from "react";
import { useRouter } from "next/navigation";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { adminSettlementService } from "@/service/admin/settlementService";
import {
  AdminPayoutResponse,
  AdminSettlementStatisticsResponse,
  AdminPayoutSearchCondition
} from "@/types/admin/settlement";

// StatCard 컴포넌트(대시보드 스타일)
const StatCard = ({ title, value, unit, icon, color }: {
  title: string;
  value: string | number;
  unit?: string;
  icon: React.ReactNode;
  color: string;
}) => (
  <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6 w-full max-w-full min-w-0 overflow-x-auto">
    <div className="flex items-center justify-between">
      <div>
        <p className="text-sm font-medium text-gray-600 mb-1">{title}</p>
        <p className="text-3xl font-bold text-gray-900">
          {value}
          {unit && <span className="text-lg ml-1 text-gray-500">{unit}</span>}
        </p>
      </div>
      <div className={`p-3 rounded-xl ${color}`}>
        {icon}
      </div>
    </div>
  </div>
);

export default function SettlementManagementPage() {
  const [settlements, setSettlements] = useState<AdminPayoutResponse[]>([]);
  const [statistics, setStatistics] = useState<AdminSettlementStatisticsResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  // 필터 상태
  const [sellerFilter, setSellerFilter] = useState("");
  const [periodStartFilter, setPeriodStartFilter] = useState("");
  const [periodEndFilter, setPeriodEndFilter] = useState("");
  
  // 페이징 상태
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize] = useState(10);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  
  const router = useRouter();

  // 인증 체크
  useEffect(() => {
    if (typeof window !== 'undefined' && !localStorage.getItem('adminToken')) {
      router.replace('/admin/login');
      return;
    }
  }, [router]);

  // 정산 목록 및 통계 데이터 로드
  const loadData = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);

      // 검색 조건 구성
      const searchCondition: AdminPayoutSearchCondition = {
        ...(sellerFilter && { sellerName: sellerFilter }),
        ...(periodStartFilter && { periodStart: periodStartFilter }),
        ...(periodEndFilter && { periodEnd: periodEndFilter })
      };

      // 정산 목록과 통계를 병렬로 로드
      const [payoutListResponse, statisticsResponse] = await Promise.all([
        adminSettlementService.getPayoutList(currentPage, pageSize, searchCondition),
        adminSettlementService.getSettlementStatistics()
      ]);

      setSettlements(payoutListResponse.content);
      setTotalElements(payoutListResponse.totalElements);
      setTotalPages(payoutListResponse.totalPages);
      setStatistics(statisticsResponse);

    } catch (err) {
      console.error('정산 데이터 로드 실패:', err);
      setError('정산 데이터를 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  }, [currentPage, pageSize, sellerFilter, periodStartFilter, periodEndFilter]);

  // 필터 변경 시 데이터 다시 로드
  useEffect(() => {
    loadData();
  }, [loadData]);

  // 필터 초기화
  const resetFilters = () => {
    setSellerFilter("");
    setPeriodStartFilter("");
    setPeriodEndFilter("");
    setCurrentPage(0);
  };

  // 페이지 변경 핸들러
  const handlePageChange = (page: number) => {
    setCurrentPage(page);
  };

  if (typeof window !== 'undefined' && !localStorage.getItem('adminToken')) {
    return null;
  }

  if (loading && !statistics) {
    return (
      <div className="w-full max-w-full min-h-screen bg-gray-50 p-2 sm:p-6 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-green-600 mx-auto mb-4"></div>
          <p className="text-gray-600">정산 데이터를 불러오는 중...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="w-full max-w-full min-h-screen bg-gray-50 p-2 sm:p-6 flex items-center justify-center">
        <div className="text-center">
          <div className="text-red-500 text-6xl mb-4">⚠️</div>
          <h3 className="text-lg font-medium text-gray-900 mb-2">오류가 발생했습니다</h3>
          <p className="text-gray-600 mb-4">{error}</p>
          <Button onClick={loadData} variant="default">
            다시 시도
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div className="w-full max-w-full min-h-screen bg-gray-50 p-2 sm:p-6 overflow-x-auto">
      <div className="w-full max-w-full">
        {/* 헤더 */}
        <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
          <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
            <div>
              <h1 className="text-2xl font-bold text-gray-900">정산 관리</h1>
              <p className="text-sm text-gray-600 mt-1">판매자 정산을 관리하고 처리할 수 있습니다.</p>
            </div>
            <div className="flex items-center gap-3">
              <div className="text-right">
                <div className="text-2xl font-bold text-green-600">{totalElements}</div>
                <div className="text-sm text-gray-500">총 정산</div>
              </div>
            </div>
          </div>
        </div>

        {/* 정산 요약 카드 */}
        {statistics && (
          <div className="grid grid-cols-1 sm:grid-cols-4 gap-4 mb-8">
            <StatCard
              title="총 정산 금액"
              value={statistics.totalPayoutAmount.toLocaleString()}
              unit="원"
              icon={<span className="text-2xl">💰</span>}
              color="bg-green-100"
            />
            <StatCard
              title="총 정산 건수"
              value={statistics.totalPayouts}
              unit="건"
              icon={<span className="text-2xl">📊</span>}
              color="bg-blue-100"
            />
            <StatCard
              title="최근 30일 정산"
              value={statistics.recentPayouts}
              unit="건"
              icon={<span className="text-2xl">📈</span>}
              color="bg-purple-100"
            />
            <StatCard
              title="최근 30일 금액"
              value={statistics.recentPayoutAmount.toLocaleString()}
              unit="원"
              icon={<span className="text-2xl">💳</span>}
              color="bg-orange-100"
            />
          </div>
        )}

        {/* 필터 */}
        <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
            <div>
              <label htmlFor="sellerFilter" className="block text-sm font-medium text-gray-700 mb-2">
                판매자 검색
              </label>
              <Input
                id="sellerFilter"
                type="text"
                placeholder="판매자명 또는 이메일로 검색"
                value={sellerFilter}
                onChange={e => setSellerFilter(e.target.value)}
              />
            </div>
            <div>
              <label htmlFor="periodStartFilter" className="block text-sm font-medium text-gray-700 mb-2">
                정산 시작일
              </label>
              <Input
                id="periodStartFilter"
                type="date"
                value={periodStartFilter}
                onChange={e => setPeriodStartFilter(e.target.value)}
              />
            </div>
            <div>
              <label htmlFor="periodEndFilter" className="block text-sm font-medium text-gray-700 mb-2">
                정산 종료일
              </label>
              <Input
                id="periodEndFilter"
                type="date"
                value={periodEndFilter}
                onChange={e => setPeriodEndFilter(e.target.value)}
              />
            </div>
            <div className="flex items-end">
              <Button 
                onClick={resetFilters}
                variant="outline"
                className="w-full"
              >
                필터 초기화
              </Button>
            </div>
          </div>
        </div>

        {/* 로딩 상태 */}
        {loading && (
          <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
            <div className="flex items-center justify-center py-8">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-green-600 mr-3"></div>
              <span className="text-gray-600">데이터를 불러오는 중...</span>
            </div>
          </div>
        )}

        {/* 데스크탑 표 */}
        {!loading && (
          <div className="hidden md:block">
            <div className="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
              <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-gray-200">
                  <thead className="bg-gray-50">
                    <tr>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider whitespace-nowrap">
                        정산ID
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider whitespace-nowrap">
                        판매자
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider whitespace-nowrap">
                        정산 기간
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider whitespace-nowrap">
                        총 판매액
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider whitespace-nowrap">
                        수수료
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider whitespace-nowrap">
                        정산 금액
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider whitespace-nowrap">
                        처리일
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider whitespace-nowrap">
                        액션
                      </th>
                    </tr>
                  </thead>
                  <tbody className="bg-white divide-y divide-gray-200">
                    {settlements.map((settlement) => (
                      <tr key={settlement.payoutId} className="hover:bg-gray-50">
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                          {settlement.payoutId}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                          <div>
                            <div className="font-medium">{settlement.sellerName}</div>
                            <div className="text-gray-500">{settlement.sellerEmail}</div>
                          </div>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          {settlement.periodStart} ~ {settlement.periodEnd}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 font-medium">
                          {settlement.totalSales.toLocaleString()}원
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          {settlement.totalCommission.toLocaleString()}원
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-green-600 font-bold">
                          {settlement.payoutAmount.toLocaleString()}원
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          {new Date(settlement.processedAt).toLocaleDateString()}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                          <Button 
                            className="text-blue-600 hover:text-blue-900 underline"
                            variant="link"
                            onClick={() => router.push(`/admin/settlement-management/${settlement.payoutId}`)}
                          >
                            상세보기
                          </Button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        )}

        {/* 모바일 카드형 리스트 */}
        {!loading && (
          <div className="block md:hidden space-y-4">
            {settlements.map((settlement, idx) => (
              <div key={settlement.payoutId} className="bg-white rounded-lg shadow-sm border border-gray-200 p-4">
                <div className="flex items-start justify-between mb-3">
                  <div>
                    <span className="text-sm font-medium text-gray-900">
                      정산ID: {settlement.payoutId}
                    </span>
                  </div>
                </div>
                
                <div className="space-y-2 mb-4">
                  <div>
                    <h3 className="font-medium text-gray-900">{settlement.sellerName}</h3>
                    <p className="text-sm text-gray-500">{settlement.sellerEmail}</p>
                  </div>
                  <div className="text-sm text-gray-600">
                    <div>정산 기간: {settlement.periodStart} ~ {settlement.periodEnd}</div>
                    <div>총 판매액: {settlement.totalSales.toLocaleString()}원</div>
                    <div>수수료: {settlement.totalCommission.toLocaleString()}원</div>
                    <div className="font-bold text-green-600">
                      정산 금액: {settlement.payoutAmount.toLocaleString()}원
                    </div>
                    <div>처리일: {new Date(settlement.processedAt).toLocaleDateString()}</div>
                  </div>
                </div>
                
                <div className="flex gap-2">
                  <Button 
                    className="flex-1"
                    variant="default"
                    onClick={() => router.push(`/admin/settlement-management/${settlement.payoutId}`)}
                  >
                    상세보기
                  </Button>
                </div>
              </div>
            ))}
          </div>
        )}

        {/* 페이징 */}
        {totalPages > 1 && (
          <div className="mt-6 flex justify-center">
            <div className="flex space-x-2">
              <button
                onClick={() => handlePageChange(currentPage - 1)}
                disabled={currentPage === 0}
                className="px-3 py-2 border rounded disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
              >
                이전
              </button>
              
              {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
                const pageNum = Math.max(0, Math.min(totalPages - 5, currentPage - 2)) + i;
                return (
                  <button
                    key={pageNum}
                    onClick={() => handlePageChange(pageNum)}
                    className={`px-3 py-2 border rounded ${
                      currentPage === pageNum 
                        ? 'bg-green-500 text-white' 
                        : 'hover:bg-gray-50'
                    }`}
                  >
                    {pageNum + 1}
                  </button>
                );
              })}
              
              <button
                onClick={() => handlePageChange(currentPage + 1)}
                disabled={currentPage === totalPages - 1}
                className="px-3 py-2 border rounded disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
              >
                다음
              </button>
            </div>
          </div>
        )}

        {/* 결과가 없을 때 */}
        {!loading && settlements.length === 0 && (
          <div className="text-center py-12">
            <div className="text-gray-400 text-6xl mb-4">💰</div>
            <h3 className="text-lg font-medium text-gray-900 mb-2">정산 내역을 찾을 수 없습니다</h3>
            <p className="text-gray-600 mb-4">
              검색 조건을 변경하거나 필터를 초기화해보세요.
            </p>
            <Button onClick={resetFilters} variant="default">
              필터 초기화
            </Button>
          </div>
        )}
      </div>
    </div>
  );
}