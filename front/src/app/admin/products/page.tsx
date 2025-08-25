"use client";
import React, { useState, useEffect } from "react";
import { Package, DollarSign, Eye } from "lucide-react";
import apiClient from "@/lib/apiClient";
import dynamic from 'next/dynamic';
import { ApexOptions } from 'apexcharts';
import { getProductQualityStats, getDailyProductRegistrationStats } from '@/service/admin/adminService';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Alert, AlertDescription } from '@/components/ui/alert';

const ReactApexChart = dynamic(() => import('react-apexcharts'), { ssr: false });

interface ProductStats {
  totalProducts: number;
  sellerProducts: number;
  adminProducts: number;
  highQualityProducts: number;    // 상
  mediumQualityProducts: number;  // 중
  lowQualityProducts: number;     // 하
  totalValue: number;
  averagePrice: number;
}

interface ProductQualityStats {
  high: number;
  medium: number;
  low: number;
  total: number;
}

interface DailyProductRegistration {
  date: string;  // "2024-06-15" 형태
  count: number;
}

export default function ProductDashboardPage() {
  const [productStats, setProductStats] = useState<any>(null);
  const [dailyStats, setDailyStats] = useState<DailyProductRegistration[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      try {
        await Promise.all([
          fetchProductStats(),
          fetchDailyStats()
        ]);
      } catch (error) {
        console.error('데이터 로딩 실패:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  const fetchProductStats = async () => {
    try {
      const stats = await getProductQualityStats();
      setProductStats(stats);
    } catch (error) {
      console.error('상품 품질별 통계 조회 실패:', error);
      setError('상품 통계를 불러오는데 실패했습니다.');
    }
  };

  const fetchDailyStats = async () => {
    try {
      const stats = await getDailyProductRegistrationStats(30); // 최근 30일
      setDailyStats(stats);
    } catch (error) {
      console.error('일별 상품 등록 통계 조회 실패:', error);
      setError('일별 통계를 불러오는데 실패했습니다.');
    }
  };

  // 통계 카드용 데이터 계산
  const stats = productStats ? {
    totalProducts: productStats.total,
    sellerProducts: productStats.total,
    adminProducts: 0,
    highQualityProducts: productStats.high,
    mediumQualityProducts: productStats.medium,
    lowQualityProducts: productStats.low,
    totalValue: productStats.total * 50000, // 임시 가치 계산
    averagePrice: 50000 // 임시 평균 가격
  } : null;

  // 품질별 분포 파이 차트 옵션
  const pieChartOptions: ApexOptions = {
    chart: {
      type: 'donut',
      height: 200,
      toolbar: {
        show: false
      }
    },
    labels: ['상', '중', '하'],
    colors: ['#10b981', '#f59e0b', '#ef4444'],
    legend: {
      position: 'bottom',
      labels: {
        colors: '#6b7280'
      }
    },
    dataLabels: {
      enabled: true,
      formatter: function(val) {
        return `${val}%`;
      },
      style: {
        fontSize: '12px',
        colors: ['#fff']
      }
    },
    tooltip: {
      theme: 'dark',
      y: {
        formatter: function(value, { series, seriesIndex, dataPointIndex, w }) {
          return `${value}개`;
        }
      }
    }
  };

  // 품질별 분포 파이 차트 데이터
  const pieChartSeries = productStats ? [
    productStats.high,
    productStats.medium,
    productStats.low
  ] : [];

  // 일별 상품 등록 추이 차트 옵션
  const dailyChartOptions: ApexOptions = {
    chart: {
      type: 'line',
      height: 350,
      toolbar: {
        show: false
      }
    },
    xaxis: {
      categories: dailyStats.map(stat => {
        const [year, month, day] = stat.date.split('-');
        return `${month}/${day}`;
      }),
      labels: {
        style: {
          colors: '#6b7280'
        }
      }
    },
    yaxis: {
      labels: {
        style: {
          colors: '#6b7280'
        }
      }
    },
    colors: ['#3b82f6'],
    stroke: {
      curve: 'smooth',
      width: 3
    },
    grid: {
      borderColor: '#e5e7eb',
      strokeDashArray: 5
    },
    tooltip: {
      theme: 'dark',
      y: {
        formatter: function(value) {
          return `${value}개`;
        }
      }
    }
  };

  const dailyChartSeries = [{
    name: '일별 상품 등록',
    data: dailyStats.map(item => item.count)
  }];

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 p-6">
        <div className="max-w-7xl mx-auto">
          <div className="animate-pulse">
            <div className="h-8 bg-gray-200 rounded w-1/4 mb-6"></div>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
              {[...Array(4)].map((_, i) => (
                <div key={i} className="bg-white rounded-lg shadow p-6">
                  <div className="h-4 bg-gray-200 rounded w-3/4 mb-4"></div>
                  <div className="h-8 bg-gray-200 rounded w-1/2"></div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gray-50 p-6">
        <div className="max-w-7xl mx-auto">
          <div className="text-center py-12">
            <div className="text-red-500 text-6xl mb-4">⚠️</div>
            <h3 className="text-lg font-medium text-gray-900 mb-2">오류가 발생했습니다</h3>
            <p className="text-gray-600">{error}</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-50 to-gray-100">
      <div className="max-w-7xl mx-auto px-2 sm:px-6 lg:px-8 py-8 w-full">
        {/* 헤더 섹션 */}
        <div className="mb-8">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-3xl font-bold text-gray-900 mb-2">상품 통계 대시보드</h1>
              <p className="text-gray-600">전체 상품 현황과 통계를 한눈에 확인하세요</p>
            </div>
            <div className="flex items-center space-x-3">
              <div className="bg-white rounded-lg px-4 py-2 shadow-sm border">
                <div className="text-sm text-gray-500">마지막 업데이트</div>
                <div className="text-sm font-medium text-gray-900">
                  {new Date().toLocaleDateString('ko-KR', { 
                    month: 'long', 
                    day: 'numeric', 
                    hour: '2-digit', 
                    minute: '2-digit' 
                  })}
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* 품질 경고 섹션 */}
        {productStats && productStats.low > productStats.high && (
          <div className="mb-8">
            <div className="bg-gradient-to-r from-red-50 to-orange-50 border border-red-200 rounded-xl p-6 shadow-sm">
              <div className="flex items-center">
                <div className="flex-shrink-0">
                  <div className="w-8 h-8 bg-red-100 rounded-full flex items-center justify-center">
                    <svg className="w-5 h-5 text-red-600" fill="currentColor" viewBox="0 0 20 20">
                      <path fillRule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
                    </svg>
                  </div>
                </div>
                <div className="ml-4">
                  <h3 className="text-lg font-semibold text-red-800">품질 관리 주의</h3>
                  <p className="text-red-700 mt-1">하품질 상품이 상품질 상품보다 많습니다. 품질 관리가 필요합니다.</p>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* 메인 통계 카드 그리드 */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8 w-full">
          {/* 전체 상품 카드 */}
          <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6 hover:shadow-md transition-shadow duration-200 w-full max-w-full min-w-0 overflow-x-auto">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600 mb-1">전체 상품</p>
                <p className="text-3xl font-bold text-gray-900">{stats?.totalProducts.toLocaleString()}</p>
                <div className="mt-2 flex items-center text-sm">
                  <span className="text-gray-500">판매자</span>
                  <span className="ml-2 font-medium text-blue-600">{stats?.sellerProducts.toLocaleString()}</span>
                  <span className="mx-2 text-gray-300">•</span>
                  <span className="text-gray-500">관리자</span>
                  <span className="ml-2 font-medium text-purple-600">{stats?.adminProducts.toLocaleString()}</span>
                </div>
              </div>
              <div className="p-3 bg-blue-50 rounded-xl">
                <Package className="w-6 h-6 text-blue-600" />
              </div>
            </div>
          </div>

          {/* 품질별 카드들 */}
          <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6 hover:shadow-md transition-shadow duration-200 w-full max-w-full min-w-0 overflow-x-auto">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600 mb-1">상</p>
                <p className="text-3xl font-bold text-green-600">{stats?.highQualityProducts.toLocaleString()}</p>
                <div className="mt-2 text-sm text-gray-500">
                  {stats?.totalProducts ? Math.round((stats.highQualityProducts / stats.totalProducts) * 100) : 0}% 비율
                </div>
              </div>
              <div className="p-3 bg-green-50 rounded-xl">
                <svg className="w-6 h-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                </svg>
              </div>
            </div>
          </div>

          <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6 hover:shadow-md transition-shadow duration-200 w-full max-w-full min-w-0 overflow-x-auto">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600 mb-1">중</p>
                <p className="text-3xl font-bold text-yellow-600">{stats?.mediumQualityProducts.toLocaleString()}</p>
                <div className="mt-2 text-sm text-gray-500">
                  {stats?.totalProducts ? Math.round((stats.mediumQualityProducts / stats.totalProducts) * 100) : 0}% 비율
                </div>
              </div>
              <div className="p-3 bg-yellow-50 rounded-xl">
                <svg className="w-6 h-6 text-yellow-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z" />
                </svg>
              </div>
            </div>
          </div>

          <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6 hover:shadow-md transition-shadow duration-200 w-full max-w-full min-w-0 overflow-x-auto">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600 mb-1">하</p>
                <p className="text-3xl font-bold text-red-600">{stats?.lowQualityProducts.toLocaleString()}</p>
                <div className="mt-2 text-sm text-gray-500">
                  {stats?.totalProducts ? Math.round((stats.lowQualityProducts / stats.totalProducts) * 100) : 0}% 비율
                </div>
              </div>
              <div className="p-3 bg-red-50 rounded-xl">
                <svg className="w-6 h-6 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </div>
            </div>
          </div>
        </div>

        {/* 가치 및 분포 섹션 */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8">
          {/* 총 상품 가치 */}
          <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6 hover:shadow-md transition-shadow duration-200">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-semibold text-gray-900">총 상품 가치</h3>
              <div className="p-2 bg-purple-50 rounded-lg">
                <DollarSign className="w-5 h-5 text-purple-600" />
              </div>
            </div>
            <div className="space-y-3">
              <div>
                <p className="text-2xl font-bold text-purple-600">
                  {stats?.totalValue.toLocaleString()}원
                </p>
                <p className="text-sm text-gray-500">전체 상품 가치</p>
              </div>
              <div className="pt-3 border-t border-gray-100">
                <div className="flex justify-between items-center">
                  <span className="text-sm text-gray-600">평균 가격</span>
                  <span className="font-medium text-gray-900">{stats?.averagePrice.toLocaleString()}원</span>
                </div>
              </div>
            </div>
          </div>

          {/* 품질별 분포 파이 차트 */}
          <div className="lg:col-span-2 bg-white rounded-xl shadow-sm border border-gray-200 p-6 hover:shadow-md transition-shadow duration-200">
            <div className="flex items-center justify-between mb-6">
              <div>
                <h3 className="text-lg font-semibold text-gray-900">품질별 분포</h3>
                <p className="text-sm text-gray-600">상품 품질별 등록 현황</p>
              </div>
              <div className="p-2 bg-blue-50 rounded-lg">
                <Eye className="w-5 h-5 text-blue-600" />
              </div>
            </div>
            {productStats && (
              <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                <div className="flex justify-center">
                  <ReactApexChart
                    options={pieChartOptions}
                    series={pieChartSeries}
                    type="donut"
                    height={200}
                  />
                </div>
                <div className="space-y-4">
                  <div className="grid grid-cols-3 gap-4">
                    <div className="text-center p-4 bg-green-50 rounded-lg">
                      <div className="text-2xl font-bold text-green-600">{productStats.high}</div>
                      <div className="text-sm text-green-700 font-medium">상</div>
                    </div>
                    <div className="text-center p-4 bg-yellow-50 rounded-lg">
                      <div className="text-2xl font-bold text-yellow-600">{productStats.medium}</div>
                      <div className="text-sm text-yellow-700 font-medium">중</div>
                    </div>
                    <div className="text-center p-4 bg-red-50 rounded-lg">
                      <div className="text-2xl font-bold text-red-600">{productStats.low}</div>
                      <div className="text-sm text-red-700 font-medium">하</div>
                    </div>
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>

        {/* 일별 상품 등록 추이 차트 */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6 hover:shadow-md transition-shadow duration-200">
          <div className="flex items-center justify-between mb-6">
            <div>
              <h3 className="text-lg font-semibold text-gray-900">일별 상품 등록 추이</h3>
              <p className="text-sm text-gray-600">최근 30일간 상품 등록 현황</p>
            </div>
            <div className="flex items-center space-x-2">
              <div className="flex items-center space-x-1">
                <div className="w-3 h-3 bg-blue-500 rounded-full"></div>
                <span className="text-sm text-gray-600">등록된 상품 수</span>
              </div>
            </div>
          </div>
          <div className="h-80">
            {dailyStats.length > 0 ? (
              <ReactApexChart
                options={dailyChartOptions}
                series={dailyChartSeries}
                type="line"
                height={320}
              />
            ) : (
              <div className="flex items-center justify-center h-full">
                <div className="text-center">
                  <div className="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
                    <svg className="w-8 h-8 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                    </svg>
                  </div>
                  <p className="text-gray-500">데이터가 없습니다</p>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}