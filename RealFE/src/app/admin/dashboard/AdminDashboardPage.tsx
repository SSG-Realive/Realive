"use client";
import React, { useEffect, useState } from 'react';
import dynamic from 'next/dynamic';
import { AdminDashboardDTO } from '@/types/admin/admin';
import { getAdminDashboard, getSalesStatistics } from '@/service/admin/adminService';
import { useRouter } from 'next/navigation';
import Modal from '@/components/Modal';
import { useAdminAuthStore } from '@/store/admin/useAdminAuthStore';
import { Users, UserCheck, UserX, UserPlus, Package, TrendingUp, DollarSign, ShoppingCart, Award, BarChart3 } from 'lucide-react';
import apiClient from '@/lib/apiClient';
import { Button } from "@/components/ui/button";

const DashboardChart = dynamic(() => import('@/components/DashboardChart'), { ssr: false });

interface ProductStats {
  totalProducts: number;
  highQualityProducts: number;
  mediumQualityProducts: number;
  lowQualityProducts: number;
  categoryStats: { [key: string]: number };
}

// 실제 DB 카테고리 구조에 맞는 대분류 매핑
const getMainCategory = (categoryName: string): string => {
  if (!categoryName) return '기타 가구';
  
  // ">" 구분자로 분리해서 첫 번째 부분(대분류) 추출
  const parts = categoryName.split(' > ').map(part => part.trim());
  const mainCategoryName = parts[0] || categoryName;
  
  // 실제 DB 카테고리 구조에 맞는 매핑
  const categoryMap: { [key: string]: string } = {
    // 대분류 직접 매핑
    '거실 가구': '거실 가구',
    '침실 가구': '침실 가구', 
    '주방·다이닝 가구': '주방·다이닝 가구',
    '서재·오피스 가구': '서재·오피스 가구',
    '기타 가구': '기타 가구',
    
    // 소분류들을 대분류로 매핑
    '소파': '거실 가구',
    '거실 테이블': '거실 가구',
    'TV·미디어장': '거실 가구',
    '진열장·책장': '거실 가구',
    
    '침대': '침실 가구',
    '매트리스': '침실 가구',
    '화장대·거울': '침실 가구',
    '옷장·행거': '침실 가구',
    '수납장·서랍장': '침실 가구',
    
    '식탁': '주방·다이닝 가구',
    '주방 의자': '주방·다이닝 가구',
    '주방 수납장': '주방·다이닝 가구',
    '아일랜드 식탁·홈바': '주방·다이닝 가구',
    
    '책상': '서재·오피스 가구',
    '사무용 의자': '서재·오피스 가구',
    '책장': '서재·오피스 가구',
    
    '현관·중문 가구': '기타 가구',
    '야외·아웃도어 가구': '기타 가구',
    '리퍼·전시가구': '기타 가구',
    'DIY·부속품': '기타 가구'
  };

  const mapped = categoryMap[mainCategoryName] || '기타 가구';
  console.log(`카테고리 처리: "${categoryName}" -> 대분류: "${mainCategoryName}" -> 매핑: "${mapped}"`);
  
  return mapped;
};

const StatCard = ({ title, value, unit, icon, color, trend }: {
  title: string;
  value: string | number;
  unit?: string;
  icon: React.ReactNode;
  color: string;
  trend?: { value: number; isPositive: boolean };
}) => (
  <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6 hover:shadow-md transition-all duration-200 w-full max-w-full min-w-0 overflow-x-auto">
    <div className="flex items-center justify-between">
      <div>
        <p className="text-sm font-medium text-gray-600 mb-1">{title}</p>
        <p className="text-3xl font-bold text-gray-900">
          {value}
          {unit && <span className="text-lg ml-1 text-gray-500">{unit}</span>}
        </p>
        {trend && (
          <div className="flex items-center mt-2">
            <span className={`text-sm font-medium ${trend.isPositive ? 'text-green-600' : 'text-red-600'}`}>
              {trend.isPositive ? '+' : ''}{trend.value}%
            </span>
            <span className="text-sm text-gray-500 ml-1">전월 대비</span>
          </div>
        )}
      </div>
      <div className={`p-3 rounded-xl ${color}`}>
        {icon}
      </div>
    </div>
  </div>
);

const MemberStatusItem = ({ icon, label, value, iconBgColor, percentage }: {
  icon: React.ReactNode;
  label: string;
  value: string | number;
  iconBgColor: string;
  percentage?: number;
}) => (
  <div className="flex items-center justify-between py-4">
    <div className="flex items-center gap-4">
      <div className={`w-10 h-10 rounded-xl flex items-center justify-center text-white ${iconBgColor}`}>
        {icon}
      </div>
      <div>
        <span className="font-semibold text-gray-700">{label}</span>
        {percentage && (
          <div className="text-sm text-gray-500">{percentage}%</div>
        )}
      </div>
    </div>
    <span className="font-bold text-lg text-gray-800">{value}</span>
  </div>
);

const AdminDashboardPage = () => {
  const [dashboardData, setDashboardData] = useState<AdminDashboardDTO | null>(null);
  const [productStats, setProductStats] = useState<ProductStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [salesLoading, setSalesLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [periodType, setPeriodType] = useState<'DAILY' | 'MONTHLY'>('DAILY');
  const [showModal, setShowModal] = useState(false);
  const [monthlySalesData, setMonthlySalesData] = useState<any[]>([]);
  const [isEmptyData, setIsEmptyData] = useState(false);
  const router = useRouter();
  const { accessToken, hydrated, initialize } = useAdminAuthStore();

  useEffect(() => {
    // URL에서 로그인 성공 메시지 확인
    const params = new URLSearchParams(window.location.search);
    const loginSuccess = params.get('loginSuccess');
    if (loginSuccess === 'true') {
      setShowModal(true);
      // URL에서 파라미터 제거
      router.replace('/admin/dashboard');
    }
  }, [router]);

  useEffect(() => {
    // 스토어가 hydrated되지 않았으면 초기화
    if (typeof window !== 'undefined' && !hydrated) {
      initialize();
        return;
      }

    // hydrated 상태이지만 토큰이 없으면 로그인 페이지로 이동
    if (hydrated && !accessToken) {
      router.replace('/admin/login');
      return;
    }
  }, [router, hydrated, accessToken, initialize]);

  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      setError(null);
      
      let data;
      
      if (periodType === 'DAILY') {
        const today = new Date().toISOString().split('T')[0];
        data = await getAdminDashboard(today, periodType);
      } else {
        const now = new Date();
        const year = now.getFullYear();
        const month = now.getMonth() + 1;
        const startDate = `${year}-${month.toString().padStart(2, '0')}-01`;
        data = await getAdminDashboard(startDate, periodType);
      }

      if (!data) {
        throw new Error('데이터를 불러오는데 실패했습니다.');
      } 

      console.log('Dashboard data received:', data);
      console.log('Period type:', periodType);
      console.log('Sales summary:', data.salesSummaryStats);
      
      setDashboardData(data);
    } catch (err) {
      console.error('Error fetching dashboard data:', err);
      if (err instanceof Error) {
        if (err.message.includes('관리자 인증이 필요합니다')) {
          router.replace('/admin/login');
          return;
        }
        setError(err.message);
      } else {
        setError('대시보드 데이터를 불러오는데 실패했습니다.');
      }
    } finally {
      setLoading(false);
    }
  };

  const fetchProductStats = async () => {
    try {
      const token = localStorage.getItem('adminToken');

      // 전체 상품 통계
      const allProductsRes = await apiClient.get('/admin/products?size=1000', {
        headers: { Authorization: `Bearer ${token}` }
      });

      const allProducts = allProductsRes.data.dtoList || [];

      // 상태별 통계 계산
      const highQualityProducts = allProducts.filter((p: any) => p.status === "상").length;
      const mediumQualityProducts = allProducts.filter((p: any) => p.status === "중").length;
      const lowQualityProducts = allProducts.filter((p: any) => p.status === "하").length;

      // 카테고리별 통계 계산 (대분류로 그룹화)
      const categoryStats: { [key: string]: number } = {};
      allProducts.forEach((product: any) => {
        const category = product.categoryName || '기타';
        const mainCategory = getMainCategory(category);
        categoryStats[mainCategory] = (categoryStats[mainCategory] || 0) + 1;
      });

      const statsData = {
        totalProducts: allProducts.length,
        highQualityProducts,
        mediumQualityProducts,
        lowQualityProducts,
        categoryStats
      };

      setProductStats(statsData);
    } catch (error) {
      console.error('Failed to fetch product stats:', error);
    }
  };

  // periodType이 바뀔 때 실행되는 useEffect
  useEffect(() => {
    const fetchData = async () => {
      // periodType이 바뀔 때 sales chart만 로딩 상태로 설정
      setSalesLoading(true);

      if (periodType === 'DAILY') {
        await fetchDashboardData();
      } else { // MONTHLY
        // 다른 차트 데이터도 월간 기준으로 업데이트
        await fetchDashboardData();

        const endDate = new Date();
        const startDate = new Date();
        startDate.setMonth(endDate.getMonth() - 5);
        const start = startDate.toISOString().split('T')[0];
        const end = endDate.toISOString().split('T')[0];

        try {
          const data = await getSalesStatistics(start, end);
          setMonthlySalesData(data?.data || []);
        } catch (error) {
          console.error('Failed to fetch monthly sales data:', error);
          setMonthlySalesData([]);
        }
      }
      setSalesLoading(false);
    };

    if (!loading) { // 첫 페이지 로딩이 아닐 때만 실행
      fetchData();
    }
  }, [periodType]);

  // 첫 페이지 로딩
  useEffect(() => {
    const initializeData = async () => {
      if (!hydrated) return;
      
      await fetchDashboardData();
      await fetchProductStats();
    };

    initializeData();
  }, [hydrated, accessToken]);

  if (loading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-gray-50 to-gray-100 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-gray-800 mx-auto mb-4"></div>
          <p className="text-gray-600">대시보드 데이터를 불러오는 중...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-gray-50 to-gray-100 flex items-center justify-center">
        <div className="bg-white rounded-xl shadow-lg p-8 max-w-md w-full mx-4">
          <div className="text-red-500 text-center mb-4">
            <svg className="w-16 h-16 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z" />
            </svg>
            <p className="text-lg font-semibold">오류가 발생했습니다</p>
            <p className="text-sm mt-2">{error}</p>
          </div>
          <Button
            onClick={fetchDashboardData}
            className="w-full"
            variant="default"
          >
            다시 시도
          </Button>
        </div>
      </div>
    );
  }

  if (!dashboardData) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-gray-50 to-gray-100 flex items-center justify-center">
        <div className="text-gray-500 text-center">
          <svg className="w-16 h-16 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
          </svg>
          <p className="text-lg">데이터가 없습니다</p>
        </div>
      </div>
    );
  }

  const totalMembers = dashboardData.memberSummaryStats?.totalMembers || 0;
  const activeMembers = dashboardData.memberSummaryStats?.activeMembers || 0;
  const activePercentage = totalMembers > 0 ? Math.round((activeMembers / totalMembers) * 100) : 0;

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-50 to-gray-100">
      <div className="max-w-7xl mx-auto px-2 sm:px-4 lg:px-8 py-8 w-full">
        {/* 로그인 성공 모달 */}
        {showModal && (
        <Modal
          isOpen={showModal}
          onClose={() => setShowModal(false)}
          title="로그인 성공"
          message="관리자 페이지에 오신 것을 환영합니다!"
          type="success"
        />
        )}

        {/* 헤더 섹션 */}
        <div className="mb-8">
          <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between items-start w-full">
            <h1 className="text-3xl font-bold text-gray-900 mb-2 whitespace-nowrap">관리자 대시보드</h1>
            <div className="flex flex-col sm:flex-row items-end sm:items-center gap-2 sm:gap-4 w-auto sm:w-auto mt-4 sm:mt-0 self-end sm:self-auto">
              <div className="bg-white rounded-lg px-4 py-2 shadow-sm border w-full sm:w-auto">
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
              <div className="flex gap-2 bg-white p-1 rounded-lg shadow-sm border w-full sm:w-auto">
                <Button
                  onClick={() => setPeriodType('DAILY')}
                  variant={periodType === 'DAILY' ? "default" : "outline"}
                  size="sm"
                  className="w-full sm:w-auto"
                >
                  일간
                </Button>
                <Button
                  onClick={() => setPeriodType('MONTHLY')}
                  variant={periodType === 'MONTHLY' ? "default" : "outline"}
                  size="sm"
                  className="w-full sm:w-auto"
                >
                  월간
                </Button>
              </div>
            </div>
          </div>
        </div>

        {/* 주요 통계 카드 */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          <StatCard
            title="총 주문"
            value={dashboardData.salesSummaryStats?.totalOrdersInPeriod || 0}
            unit="건"
            icon={<ShoppingCart className="w-6 h-6 text-white" />}
            color="bg-blue-500"
          />
          <StatCard
            title={periodType === 'DAILY' ? '일일 매출' : '월간 매출'}
            value={dashboardData.salesSummaryStats?.totalRevenueInPeriod?.toLocaleString() || "0"}
            unit="원"
            icon={<DollarSign className="w-6 h-6 text-white" />}
            color="bg-green-500"
          />
          <StatCard
            title="활성 회원"
            value={activeMembers}
            unit="명"
            icon={<Users className="w-6 h-6 text-white" />}
            color="bg-purple-500"
          />
          <StatCard
            title="전체 상품"
            value={productStats?.totalProducts || 0}
            unit="개"
            icon={<Package className="w-6 h-6 text-white" />}
            color="bg-orange-500"
          />
        </div>

        {/* 상세 통계 섹션 */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8">
          {/* 회원 현황 */}
          <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6 hover:shadow-md transition-shadow duration-200">
            <div className="flex items-center justify-between mb-6">
              <h3 className="text-lg font-semibold text-gray-900">회원 현황</h3>
              <div className="p-2 bg-purple-50 rounded-lg">
                <Users className="w-5 h-5 text-purple-600" />
              </div>
            </div>
            <div className="space-y-4">
              <MemberStatusItem
                icon={<Users size={20} />}
                label="전체 회원"
                value={dashboardData.memberSummaryStats?.totalMembers || 0}
                iconBgColor="bg-purple-500"
              />
              <MemberStatusItem
                icon={<UserCheck size={20} />}
                label="활성 회원"
                value={dashboardData.memberSummaryStats?.activeMembers || 0}
                iconBgColor="bg-green-500"
                percentage={activePercentage}
              />
              <MemberStatusItem
                icon={<UserX size={20} />}
                label="비활성 회원"
                value={dashboardData.memberSummaryStats?.inactiveMembers || 0}
                iconBgColor="bg-red-500"
              />
              <MemberStatusItem
                icon={<UserPlus size={20} />}
                label="대기 판매자"
                value={dashboardData.pendingSellerCount || 0}
                iconBgColor="bg-orange-500"
              />
            </div>
          </div>

          {/* 상품 품질 현황 */}
          {productStats && (
            <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6 hover:shadow-md transition-shadow duration-200">
              <div className="flex items-center justify-between mb-6">
                <h3 className="text-lg font-semibold text-gray-900">상품 품질 현황</h3>
                <div className="p-2 bg-blue-50 rounded-lg">
                  <Award className="w-5 h-5 text-blue-600" />
                </div>
              </div>
              <div className="space-y-4">
                <MemberStatusItem
                  icon={<Package size={20} />}
                  label="전체 상품"
                  value={productStats.totalProducts}
                  iconBgColor="bg-blue-500"
                />
                <MemberStatusItem
                  icon={<Package size={20} />}
                  label="상"
                  value={productStats.highQualityProducts}
                  iconBgColor="bg-green-500"
                  percentage={productStats.totalProducts > 0 ? Math.round((productStats.highQualityProducts / productStats.totalProducts) * 100) : 0}
                />
                <MemberStatusItem
                  icon={<Package size={20} />}
                  label="중"
                  value={productStats.mediumQualityProducts}
                  iconBgColor="bg-yellow-500"
                  percentage={productStats.totalProducts > 0 ? Math.round((productStats.mediumQualityProducts / productStats.totalProducts) * 100) : 0}
                />
                <MemberStatusItem
                  icon={<Package size={20} />}
                  label="하"
                  value={productStats.lowQualityProducts}
                  iconBgColor="bg-red-500"
                  percentage={productStats.totalProducts > 0 ? Math.round((productStats.lowQualityProducts / productStats.totalProducts) * 100) : 0}
                />
              </div>
            </div>
          )}

          {/* 경매 현황 */}
          <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6 hover:shadow-md transition-shadow duration-200">
            <div className="flex items-center justify-between mb-6">
              <h3 className="text-lg font-semibold text-gray-900">경매 현황</h3>
              <div className="p-2 bg-indigo-50 rounded-lg">
                <TrendingUp className="w-5 h-5 text-indigo-600" />
              </div>
            </div>
            <div className="space-y-4">
              <MemberStatusItem
                icon={<TrendingUp size={20} />}
                label="총 경매"
                value={dashboardData.auctionSummaryStats?.totalAuctionsInPeriod || 0}
                iconBgColor="bg-indigo-500"
              />
              <MemberStatusItem
                icon={<TrendingUp size={20} />}
                label="총 입찰"
                value={dashboardData.auctionSummaryStats?.totalBidsInPeriod || 0}
                iconBgColor="bg-blue-500"
              />
              <MemberStatusItem
                icon={<TrendingUp size={20} />}
                label="평균 입찰"
                value={dashboardData.auctionSummaryStats?.averageBidsPerAuctionInPeriod?.toFixed(1) || 0}
                iconBgColor="bg-green-500"
              />
            </div>
          </div>
        </div>

        {/* 차트 섹션 */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
          {/* 매출 추이 */}
          <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6 hover:shadow-md transition-shadow duration-200">
            <div className="flex items-center justify-between mb-6">
              <h3 className="text-lg font-semibold text-gray-900">매출 추이</h3>
              <div className="p-2 bg-green-50 rounded-lg">
                <DollarSign className="w-5 h-5 text-green-600" />
              </div>
            </div>
            <div style={{ height: '350px' }}>
              {salesLoading ? (
                <div className="flex items-center justify-center h-full">
                  <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-gray-800"></div>
                </div>
              ) : (
                <DashboardChart
                  data={dashboardData}
                  type="sales"
                  periodType={periodType}
                />
              )}
            </div>
          </div>

          {/* 회원 통계 */}
          <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6 hover:shadow-md transition-shadow duration-200">
            <div className="flex items-center justify-between mb-6">
              <h3 className="text-lg font-semibold text-gray-900">회원 통계</h3>
              <div className="p-2 bg-purple-50 rounded-lg">
                <Users className="w-5 h-5 text-purple-600" />
              </div>
            </div>
            <div style={{ height: '350px' }}>
              <DashboardChart data={dashboardData} type="member" />
            </div>
          </div>
        </div>

        {/* 추가 차트 섹션 */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* 경매 통계 */}
          <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6 hover:shadow-md transition-shadow duration-200">
            <div className="flex items-center justify-between mb-6">
              <h3 className="text-lg font-semibold text-gray-900">경매 통계</h3>
              <div className="p-2 bg-indigo-50 rounded-lg">
                <TrendingUp className="w-5 h-5 text-indigo-600" />
              </div>
            </div>
            <div style={{ height: '300px' }}>
              <DashboardChart data={dashboardData} type="auction" />
            </div>
          </div>

          {/* 상품 품질 분포 */}
          <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6 hover:shadow-md transition-shadow duration-200">
            <div className="flex items-center justify-between mb-6">
              <h3 className="text-lg font-semibold text-gray-900">상품 품질 분포</h3>
              <div className="p-2 bg-blue-50 rounded-lg">
                <Award className="w-5 h-5 text-blue-600" />
              </div>
            </div>
            <div style={{ height: '300px' }}>
              {productStats && <DashboardChart data={productStats} type="product" />}
            </div>
          </div>

          {/* 카테고리별 상품 분포 (대분류) */}
          <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6 hover:shadow-md transition-shadow duration-200">
            <div className="flex items-center justify-between mb-6">
              <h3 className="text-lg font-semibold text-gray-900">카테고리별 분포</h3>
              <div className="p-2 bg-orange-50 rounded-lg">
                <BarChart3 className="w-5 h-5 text-orange-600" />
              </div>
            </div>
            <div style={{ height: '300px' }}>
              {productStats && <DashboardChart data={productStats} type="category" />}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AdminDashboardPage;