"use client";
import React, { useEffect, useState } from 'react';
import dynamic from 'next/dynamic';
import { AdminDashboardDTO } from '@/types/admin/admin';
import { getAdminDashboard } from '@/service/admin/adminService';
import { useRouter } from 'next/navigation';
import Modal from '@/components/Modal';
import { useAdminAuthStore } from '@/store/admin/useAdminAuthStore';
import { Users, UserCheck, UserX, UserPlus } from 'lucide-react';

const DashboardChart = dynamic(() => import('@/components/DashboardChart'), { ssr: false });

const StatCard = ({ title, value, unit }: { title: string; value: string | number; unit?: string }) => (
  <div className="bg-white/50 p-6 rounded-xl">
    <h4 className="text-base font-semibold text-stone-700 mb-2">{title}</h4>
    <p className="text-4xl font-bold text-stone-800">
      {value}
      {unit && <span className="text-2xl ml-1">{unit}</span>}
    </p>
  </div>
);

const MemberStatusItem = ({ icon, label, value, iconBgColor }: { icon: React.ReactNode; label: string; value: string | number; iconBgColor: string }) => (
  <div className="flex items-center justify-between py-3">
    <div className="flex items-center gap-4">
      <div className={`w-10 h-10 rounded-lg flex items-center justify-center text-white ${iconBgColor}`}>
        {icon}
      </div>
      <span className="font-semibold text-gray-700">{label}</span>
    </div>
    <span className="font-bold text-lg text-gray-800">{value}</span>
  </div>
);

const AdminDashboardPage = () => {
  const [dashboardData, setDashboardData] = useState<AdminDashboardDTO | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [periodType, setPeriodType] = useState<'DAILY' | 'MONTHLY'>('DAILY');
  const [showModal, setShowModal] = useState(false);
  const router = useRouter();

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
    if (typeof window !== 'undefined') {
      const adminToken = localStorage.getItem('adminToken');
      const storeToken = useAdminAuthStore.getState().accessToken;
      
      // localStorage나 Zustand 스토어에 토큰이 없으면 로그인 페이지로 이동
      if (!adminToken && !storeToken) {
        router.replace('/admin/login');
        return;
      }
      
      // localStorage에 토큰이 있지만 Zustand 스토어에 없으면 동기화
      if (adminToken && !storeToken) {
        const refreshToken = localStorage.getItem('adminRefreshToken');
        useAdminAuthStore.getState().setTokens(adminToken, refreshToken || '');
      }
    }
  }, [router]);

  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      setError(null);
      const today = new Date().toISOString().split('T')[0];
      const data = await getAdminDashboard(today, periodType);
      
      if (!data) {
        throw new Error('데이터를 불러오는데 실패했습니다.');
      }

      console.log('Dashboard Data:', data);
      console.log('Member Summary Stats:', data.memberSummaryStats);
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

  useEffect(() => {
    fetchDashboardData();
  }, [periodType]);

  if (loading) {
    return (
        <div className="flex items-center justify-center h-full">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-gray-800"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="text-red-500 bg-red-100 p-4 rounded-lg">Error: {error}</div>
    );
  }

  if (!dashboardData) {
    return <div className="text-gray-500">데이터가 없습니다.</div>;
  }

  return (
    <div>
      <Modal
        isOpen={showModal}
        onClose={() => setShowModal(false)}
        title="로그인 성공"
        message="관리자 페이지에 오신 것을 환영합니다!"
        type="success"
      />
      <div className="flex justify-between items-start mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-800">대시보드</h1>
          <p className="text-sm text-gray-500">오늘의 현황을 요약합니다.</p>
        </div>
        <div className="flex gap-2 bg-white p-1 rounded-lg shadow-sm">
            <button
              onClick={() => setPeriodType('DAILY')}
            className={`px-3 py-1.5 rounded-md text-sm font-semibold transition-colors ${
                periodType === 'DAILY'
                ? 'bg-gray-800 text-white'
                : 'text-gray-600 hover:bg-gray-100'
              }`}
            >
              일간
            </button>
            <button
              onClick={() => setPeriodType('MONTHLY')}
            className={`px-3 py-1.5 rounded-md text-sm font-semibold transition-colors ${
                periodType === 'MONTHLY'
                ? 'bg-gray-800 text-white'
                : 'text-gray-600 hover:bg-gray-100'
              }`}
            >
              월간
            </button>
          </div>
        </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 p-6 rounded-2xl shadow-lg flex flex-col justify-between" style={{ backgroundColor: '#EDE7E3' }}>
          <div>
            <h3 className="text-xl font-bold text-stone-800 mb-1">주요 현황 요약</h3>
            <p className="text-sm text-stone-600 mb-6">{periodType === 'DAILY' ? '오늘' : '이번 달'}의 주문 및 매출 현황입니다.</p>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <StatCard 
              title="총 주문" 
              value={dashboardData.salesSummaryStats?.totalOrdersInPeriod || 0}
              unit="건"
            />
            <StatCard 
              title="총 매출"
              value={dashboardData.salesSummaryStats?.totalRevenueInPeriod?.toLocaleString() || 0}
              unit="원"
            />
          </div>
        </div>

        <div className="bg-white p-6 rounded-2xl shadow-lg">
          <h3 className="text-xl font-bold text-gray-800 mb-2">회원 현황</h3>
          <div className="divide-y divide-gray-100">
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
        
        <div className="lg:col-span-3 grid grid-cols-1 md:grid-cols-2 gap-6">
          <div className="bg-white p-6 rounded-2xl shadow-lg">
            <h3 className="text-xl font-bold text-gray-800 mb-4">매출 추이</h3>
            <div style={{ height: '350px' }}>
              <DashboardChart data={dashboardData} type="sales" />
            </div>
          </div>
          <div className="bg-white p-6 rounded-2xl shadow-lg">
            <h3 className="text-xl font-bold text-gray-800 mb-4">회원 통계</h3>
            <div style={{ height: '350px' }}>
              <DashboardChart data={dashboardData} type="member" />
          </div>
          </div>
          <div className="bg-white p-6 rounded-2xl shadow-lg">
            <h3 className="text-xl font-bold text-gray-800 mb-4">경매 통계</h3>
            <div style={{ height: '350px' }}>
              <DashboardChart data={dashboardData} type="auction" />
        </div>
          </div>
          <div className="bg-white p-6 rounded-2xl shadow-lg">
            <h3 className="text-xl font-bold text-gray-800 mb-4">리뷰 통계</h3>
            <div style={{ height: '350px' }}>
              <DashboardChart data={dashboardData} type="review" />
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AdminDashboardPage;