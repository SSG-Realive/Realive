"use client";
import React, { useState, useEffect } from "react";
import { Package, DollarSign, Eye, TrendingUp, AlertTriangle, CheckCircle } from "lucide-react";
import dynamic from 'next/dynamic';
import { ApexOptions } from 'apexcharts';
import { getProductQualityStats, getDailyProductRegistrationStats } from '@/service/admin/adminService';

const ReactApexChart = dynamic(() => import('react-apexcharts'), { ssr: false });

interface DailyProductRegistration {
  date: string;
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
    sellerProducts: productStats.sellerProducts,
    adminProducts: productStats.adminProducts,
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
      height: 250,
      toolbar: {
        show: false
      }
    },
    labels: ['상', '중', '하'],
    colors: ['#10b981', '#f59e0b', '#ef4444'],
    legend: {
      show: false
    },
    dataLabels: {
      enabled: false
    },
    plotOptions: {
      pie: {
        donut: {
          size: '60%',
          labels: {
            show: false
          }
        }
      }
    },
    tooltip: {
      theme: 'light',
      y: {
        formatter: function(value) {
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
      theme: 'light',
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
      <div className="min-h-screen bg-white flex items-center justify-center">
        <div className="text-center">
          <div className="relative">
            <div className="w-16 h-16 border-4 border-gray-200 border-t-gray-600 rounded-full animate-spin mx-auto mb-4"></div>
            <div className="absolute inset-0 w-16 h-16 border-4 border-transparent border-t-gray-400 rounded-full animate-spin mx-auto" style={{ animationDelay: '0.5s' }}></div>
          </div>
          <p className="text-gray-600 text-lg font-medium">상품 통계를 불러오는 중...</p>
          <div className="mt-4 flex justify-center space-x-2">
            <div className="w-2 h-2 bg-gray-500 rounded-full animate-bounce"></div>
            <div className="w-2 h-2 bg-gray-500 rounded-full animate-bounce" style={{ animationDelay: '0.1s' }}></div>
            <div className="w-2 h-2 bg-gray-500 rounded-full animate-bounce" style={{ animationDelay: '0.2s' }}></div>
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-white flex items-center justify-center">
        <div className="text-center">
          <div className="w-32 h-32 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-8">
            <AlertTriangle className="w-16 h-16 text-red-500" />
          </div>
          <h3 className="text-2xl font-bold text-gray-800 mb-4">오류가 발생했습니다</h3>
          <p className="text-gray-600 mb-8 text-lg">{error}</p>
          <button 
            onClick={() => window.location.reload()}
            className="px-8 py-4 bg-gray-800 text-white rounded-2xl font-semibold hover:bg-gray-700 transition-all duration-300"
          >
            다시 시도
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-white">
      <div className="max-w-7xl mx-auto p-6">
        {/* 헤더 섹션 */}
        <div className="mb-8">
          <div className="flex items-center justify-between mb-6">
            <div className="flex items-center space-x-4">
              <div className="relative">
                <div className="w-16 h-16 bg-gray-800 rounded-2xl flex items-center justify-center shadow-lg">
                  <TrendingUp className="w-8 h-8 text-white" />
                </div>
              </div>
              <div>
                <h1 className="text-4xl font-bold text-gray-800">
                  상품 통계 대시보드
                </h1>
                <p className="text-gray-600 mt-1">전체 상품 현황과 통계를 한눈에 확인하세요</p>
              </div>
            </div>
            <div className="bg-gray-50 rounded-2xl px-6 py-3 border border-gray-200">
              <div className="text-gray-600 text-sm">마지막 업데이트</div>
              <div className="text-lg font-bold text-gray-800">
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

        {/* 품질 경고 섹션 */}
        {productStats && productStats.low > productStats.high && (
          <div className="mb-8">
            <div className="bg-red-50 border border-red-200 rounded-3xl p-6">
              <div className="flex items-center">
                <div className="flex-shrink-0">
                  <div className="w-12 h-12 bg-red-100 rounded-full flex items-center justify-center">
                    <AlertTriangle className="w-6 h-6 text-red-600" />
                  </div>
                </div>
                <div className="ml-4">
                  <h3 className="text-lg font-semibold text-red-800">품질 관리 주의</h3>
                  <p className="text-red-700 mt-1">품질이 "하"인 상품이 "상" 상품보다 많습니다. 품질 관리가 필요합니다.</p>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* 메인 통계 카드 그리드 */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          {/* 전체 상품 카드 */}
          <div className="bg-gray-50 rounded-3xl border border-gray-200 p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600 mb-1">전체 상품</p>
                <p className="text-3xl font-bold text-gray-800">{stats?.totalProducts.toLocaleString()}</p>
                <div className="mt-2 flex items-center text-sm">
                  <span className="text-gray-500">판매자</span>
                  <span className="ml-2 font-medium text-blue-600">{stats?.sellerProducts.toLocaleString()}</span>
                  <span className="mx-2 text-gray-300">•</span>
                  <span className="text-gray-500">관리자</span>
                  <span className="ml-2 font-medium text-gray-600">{stats?.adminProducts.toLocaleString()}</span>
                </div>
              </div>
              <div className="p-3 bg-blue-100 rounded-2xl">
                <Package className="w-6 h-6 text-blue-600" />
              </div>
            </div>
          </div>

          {/* 품질별 카드들 */}
          <div className="bg-gray-50 rounded-3xl border border-gray-200 p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600 mb-1">상품질</p>
                <p className="text-3xl font-bold text-green-600">{stats?.highQualityProducts.toLocaleString()}</p>
                <div className="mt-2 text-sm text-gray-500">
                  {stats?.totalProducts ? Math.round((stats.highQualityProducts / stats.totalProducts) * 100) : 0}% 비율
                </div>
              </div>
              <div className="p-3 bg-green-100 rounded-2xl">
                <CheckCircle className="w-6 h-6 text-green-600" />
              </div>
            </div>
          </div>

          <div className="bg-gray-50 rounded-3xl border border-gray-200 p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600 mb-1">중품질</p>
                <p className="text-3xl font-bold text-yellow-600">{stats?.mediumQualityProducts.toLocaleString()}</p>
                <div className="mt-2 text-sm text-gray-500">
                  {stats?.totalProducts ? Math.round((stats.mediumQualityProducts / stats.totalProducts) * 100) : 0}% 비율
                </div>
              </div>
              <div className="p-3 bg-yellow-100 rounded-2xl">
                <AlertTriangle className="w-6 h-6 text-yellow-600" />
              </div>
            </div>
          </div>

          <div className="bg-gray-50 rounded-3xl border border-gray-200 p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600 mb-1">하품질</p>
                <p className="text-3xl font-bold text-red-600">{stats?.lowQualityProducts.toLocaleString()}</p>
                <div className="mt-2 text-sm text-gray-500">
                  {stats?.totalProducts ? Math.round((stats.lowQualityProducts / stats.totalProducts) * 100) : 0}% 비율
                </div>
              </div>
              <div className="p-3 bg-red-100 rounded-2xl">
                <AlertTriangle className="w-6 h-6 text-red-600" />
              </div>
            </div>
          </div>
        </div>

        {/* 가치 및 분포 섹션 */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8">
          {/* 총 상품 가치 */}
          <div className="bg-gray-50 rounded-3xl border border-gray-200 p-6">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-semibold text-gray-800">총 상품 가치</h3>
              <div className="p-2 bg-purple-100 rounded-xl">
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
              <div className="pt-3 border-t border-gray-200">
                <div className="flex justify-between items-center">
                  <span className="text-sm text-gray-600">평균 가격</span>
                  <span className="font-medium text-gray-800">{stats?.averagePrice.toLocaleString()}원</span>
                </div>
              </div>
            </div>
          </div>

          {/* 품질별 분포 파이 차트 */}
          <div className="lg:col-span-2 bg-gray-50 rounded-3xl border border-gray-200 p-6">
            <div className="flex items-center justify-between mb-6">
              <div>
                <h3 className="text-lg font-semibold text-gray-800">품질별 분포</h3>
                <p className="text-sm text-gray-600">상품 품질별 등록 현황</p>
              </div>
              <div className="p-2 bg-blue-100 rounded-xl">
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
                  <div className="space-y-3">
                    <div className="flex items-center justify-between p-3 bg-green-100 rounded-xl">
                      <div className="flex items-center">
                        <div className="w-3 h-3 bg-green-500 rounded-full mr-3"></div>
                        <span className="font-medium text-green-700">상품질</span>
                      </div>
                      <div className="text-right">
                        <div className="text-lg font-bold text-green-600">{productStats.high}개</div>
                        <div className="text-sm text-green-600">
                          {productStats.total > 0 ? ((productStats.high / productStats.total) * 100).toFixed(1) : 0}%
                        </div>
                      </div>
                    </div>
                    <div className="flex items-center justify-between p-3 bg-yellow-100 rounded-xl">
                      <div className="flex items-center">
                        <div className="w-3 h-3 bg-yellow-500 rounded-full mr-3"></div>
                        <span className="font-medium text-yellow-700">중품질</span>
                      </div>
                      <div className="text-right">
                        <div className="text-lg font-bold text-yellow-600">{productStats.medium}개</div>
                        <div className="text-sm text-yellow-600">
                          {productStats.total > 0 ? ((productStats.medium / productStats.total) * 100).toFixed(1) : 0}%
                        </div>
                      </div>
                    </div>
                    <div className="flex items-center justify-between p-3 bg-red-100 rounded-xl">
                      <div className="flex items-center">
                        <div className="w-3 h-3 bg-red-500 rounded-full mr-3"></div>
                        <span className="font-medium text-red-700">하품질</span>
                      </div>
                      <div className="text-right">
                        <div className="text-lg font-bold text-red-600">{productStats.low}개</div>
                        <div className="text-sm text-red-600">
                          {productStats.total > 0 ? ((productStats.low / productStats.total) * 100).toFixed(1) : 0}%
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>

        {/* 일별 상품 등록 추이 차트 */}
        <div className="bg-gray-50 rounded-3xl border border-gray-200 p-6">
          <div className="flex items-center justify-between mb-6">
            <div>
              <h3 className="text-lg font-semibold text-gray-800">일별 상품 등록 추이</h3>
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
                  <div className="w-16 h-16 bg-gray-200 rounded-full flex items-center justify-center mx-auto mb-4">
                    <TrendingUp className="w-8 h-8 text-gray-400" />
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