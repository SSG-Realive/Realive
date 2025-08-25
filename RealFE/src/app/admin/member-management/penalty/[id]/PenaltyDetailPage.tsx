"use client";
import { useParams, useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import apiClient from '@/lib/apiClient';
import { AlertTriangle, ArrowLeft, User, Calendar, FileText, Hash } from 'lucide-react';

export default function PenaltyDetailPage() {
  const params = useParams();
  const router = useRouter();
  const { id } = params;

  const [penalty, setPenalty] = useState<any>(null);
  const [customers, setCustomers] = useState<{id: number, name: string, email: string}[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = typeof window !== 'undefined' ? localStorage.getItem('adminToken') : '';
    
    // 패널티 상세 정보 로딩
    apiClient.get(`/admin/penalties/${id}`, {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      }
    })
      .then(res => {
        setPenalty(res.data);
        setLoading(false);
      })
      .catch(() => {
        setPenalty(null);
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
  }, [id]);

  const getCustomerName = (customerId: number) => {
    const c = customers.find(c => c.id === customerId);
    return c ? `${c.name} (${c.email})` : customerId;
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-slate-50 to-slate-100">
        <div className="max-w-2xl mx-auto p-4 sm:p-6 lg:p-8">
          <div className="flex items-center justify-center py-16">
            <div className="text-center">
              <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-red-600 mx-auto mb-4"></div>
              <p className="text-gray-600">패널티 정보를 불러오는 중...</p>
            </div>
          </div>
        </div>
      </div>
    );
  }

  if (!penalty) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-slate-50 to-slate-100">
        <div className="max-w-2xl mx-auto p-4 sm:p-6 lg:p-8">
          <div className="flex items-center justify-center py-16">
            <div className="text-center">
              <AlertTriangle className="w-16 h-16 text-gray-300 mx-auto mb-4" />
              <div className="text-gray-500 text-lg font-medium mb-2">패널티를 찾을 수 없습니다</div>
              <div className="text-gray-400 text-sm">요청하신 패널티 정보가 존재하지 않습니다.</div>
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 to-slate-100">
      <div className="max-w-2xl mx-auto p-4 sm:p-6 lg:p-8">
        {/* 헤더 */}
        <div className="mb-8">
          <div className="flex items-center gap-3 mb-4">
            <div className="p-3 bg-red-600 rounded-xl">
              <AlertTriangle className="w-8 h-8 text-white" />
            </div>
            <div>
              <h1 className="text-3xl font-bold text-gray-900">패널티 상세</h1>
              <p className="text-gray-600 mt-1">패널티 정보를 확인합니다</p>
            </div>
          </div>
        </div>

        {/* 패널티 상세 정보 */}
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-8">
          {/* 사용자 정보 */}
          <div className="flex items-center gap-4 mb-8 p-6 bg-gray-50 rounded-xl">
            <div className="w-16 h-16 bg-red-100 rounded-full flex items-center justify-center">
              <User className="w-8 h-8 text-red-600" />
            </div>
            <div>
              <h2 className="text-xl font-semibold text-gray-900">{getCustomerName(penalty.customerId)}</h2>
              <p className="text-gray-500">패널티 대상 사용자</p>
            </div>
          </div>

          {/* 패널티 정보 */}
          <div className="space-y-6">
            {/* 패널티 사유 */}
            <div className="flex items-start gap-4 p-4 border border-gray-200 rounded-xl">
              <div className="w-10 h-10 bg-blue-100 rounded-lg flex items-center justify-center flex-shrink-0">
                <FileText className="w-5 h-5 text-blue-600" />
              </div>
              <div className="flex-1">
                <h3 className="text-sm font-medium text-gray-700 mb-1">패널티 사유</h3>
                <p className="text-gray-900">{penalty.reason || '사유 없음'}</p>
              </div>
            </div>

            {/* 패널티 포인트 */}
            <div className="flex items-start gap-4 p-4 border border-gray-200 rounded-xl">
              <div className="w-10 h-10 bg-orange-100 rounded-lg flex items-center justify-center flex-shrink-0">
                <Hash className="w-5 h-5 text-orange-600" />
              </div>
              <div className="flex-1">
                <h3 className="text-sm font-medium text-gray-700 mb-1">패널티 포인트</h3>
                <p className="text-gray-900">{penalty.points || 0}점</p>
              </div>
            </div>

            {/* 패널티 일자 */}
            <div className="flex items-start gap-4 p-4 border border-gray-200 rounded-xl">
              <div className="w-10 h-10 bg-green-100 rounded-lg flex items-center justify-center flex-shrink-0">
                <Calendar className="w-5 h-5 text-green-600" />
              </div>
              <div className="flex-1">
                <h3 className="text-sm font-medium text-gray-700 mb-1">패널티 일자</h3>
                <p className="text-gray-900">
                  {penalty.createdAt ? (() => {
                    try {
                      const date = new Date(penalty.createdAt);
                      return isNaN(date.getTime()) ? penalty.createdAt : date.toLocaleDateString();
                    } catch {
                      return penalty.createdAt;
                    }
                  })() : '날짜 없음'}
                </p>
              </div>
            </div>

            {/* 상세 설명 - reason과 다른 경우에만 표시 */}
            {penalty.description && penalty.description !== penalty.reason && (
              <div className="flex items-start gap-4 p-4 border border-gray-200 rounded-xl">
                <div className="w-10 h-10 bg-purple-100 rounded-lg flex items-center justify-center flex-shrink-0">
                  <FileText className="w-5 h-5 text-purple-600" />
                </div>
                <div className="flex-1">
                  <h3 className="text-sm font-medium text-gray-700 mb-1">상세 설명</h3>
                  <p className="text-gray-900">{penalty.description}</p>
                </div>
              </div>
            )}
          </div>

          {/* 액션 버튼 */}
          <div className="mt-8 pt-6 border-t border-gray-200">
            <button 
              onClick={() => router.push('/admin/member-management/penalty')}
              className="w-full bg-gray-600 text-white rounded-xl px-6 py-3 hover:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-gray-500 focus:ring-offset-2 transition-all flex items-center justify-center gap-2"
            >
              <ArrowLeft className="w-5 h-5" />
              목록으로 돌아가기
            </button>
          </div>
        </div>
      </div>
    </div>
  );
} 