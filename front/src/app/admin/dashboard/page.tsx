"use client";
import React, { useEffect, useState } from 'react';
import Link from 'next/link';
import dynamic from 'next/dynamic';
import { AdminDashboardDTO } from '@/types/admin/admin';
import { getAdminDashboard } from '@/service/admin/adminService';
import { useRouter } from 'next/navigation';
import Modal from '@/components/Modal';
import { useAdminAuthStore } from '@/store/admin/useAdminAuthStore';

const DashboardChart = dynamic(() => import('@/components/DashboardChart'), { ssr: false });

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
      <div className="min-h-screen bg-gray-50 p-6">
        <div className="flex items-center justify-center h-full">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500"></div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gray-50 p-6">
        <div className="text-red-500">Error: {error}</div>
        <button 
          onClick={fetchDashboardData}
          className="mt-4 px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
        >
          Retry
        </button>
      </div>
    );
  }

  if (!dashboardData) {
    return (
      <div className="min-h-screen bg-gray-50 p-6">
        <div className="text-gray-500">데이터가 없습니다.</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 p-6">
      <Modal
        isOpen={showModal}
        onClose={() => setShowModal(false)}
        title="로그인 성공"
        message="관리자 페이지에 오신 것을 환영합니다!"
        type="success"
      />
      <div className="max-w-7xl mx-auto">
        <div className="flex justify-between items-center mb-6">
          <h1 className="text-2xl font-bold text-gray-900">관리자 대시보드</h1>
          <div className="flex gap-4">
            <button
              onClick={() => setPeriodType('DAILY')}
              className={`px-4 py-2 rounded ${
                periodType === 'DAILY'
                  ? 'bg-blue-500 text-white'
                  : 'bg-white text-gray-700'
              }`}
            >
              일간
            </button>
            <button
              onClick={() => setPeriodType('MONTHLY')}
              className={`px-4 py-2 rounded ${
                periodType === 'MONTHLY'
                  ? 'bg-blue-500 text-white'
                  : 'bg-white text-gray-700'
              }`}
            >
              월간
            </button>
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-6">
          <div className="bg-white p-6 rounded-lg shadow">
            <h3 className="text-lg font-semibold text-gray-900 mb-2">총 주문</h3>
            <p className="text-3xl font-bold text-blue-600">
              {dashboardData.salesSummaryStats?.totalOrdersInPeriod || 0}
            </p>
          </div>
          <div className="bg-white p-6 rounded-lg shadow">
            <h3 className="text-lg font-semibold text-gray-900 mb-2">총 매출</h3>
            <p className="text-3xl font-bold text-green-600">
              {dashboardData.salesSummaryStats?.totalRevenueInPeriod?.toLocaleString() || 0}원
            </p>
          </div>
          <div className="bg-white p-6 rounded-lg shadow">
            <h3 className="text-lg font-semibold text-gray-900 mb-2">전체 회원</h3>
            <p className="text-3xl font-bold text-purple-600">
              {dashboardData.memberSummaryStats?.totalMembers || 0}
            </p>
          </div>
          <div className="bg-white p-6 rounded-lg shadow">
            <h3 className="text-lg font-semibold text-gray-900 mb-2">활성 회원</h3>
            <p className="text-3xl font-bold text-green-600">
              {dashboardData.memberSummaryStats?.activeMembers || 0}
            </p>
          </div>
          <div className="bg-white p-6 rounded-lg shadow">
            <h3 className="text-lg font-semibold text-gray-900 mb-2">비활성 회원</h3>
            <p className="text-3xl font-bold text-red-600">
              {dashboardData.memberSummaryStats?.inactiveMembers || 0}
            </p>
          </div>
          <div className="bg-white p-6 rounded-lg shadow">
            <h3 className="text-lg font-semibold text-gray-900 mb-2">대기 판매자</h3>
            <p className="text-3xl font-bold text-orange-600">
              {dashboardData.pendingSellerCount || 0}
            </p>
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <div className="bg-white p-6 rounded-lg shadow">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">매출 추이</h3>
            <DashboardChart
              data={dashboardData}
            />
          </div>
          <div className="bg-white p-6 rounded-lg shadow">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">회원 통계</h3>
            <DashboardChart
              data={dashboardData}
            />
          </div>
        </div>
      </div>
    </div>
  );
};

export default AdminDashboardPage;