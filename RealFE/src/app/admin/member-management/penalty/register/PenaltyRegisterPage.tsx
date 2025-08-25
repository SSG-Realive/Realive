"use client";
import apiClient from '@/lib/apiClient';
import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { AlertTriangle, Save, ArrowLeft, User, FileText, Hash } from 'lucide-react';
import { useGlobalDialog } from '@/app/context/dialogContext';

export default function PenaltyRegisterPage() {
  const router = useRouter();
  const [customerId, setCustomerId] = useState("");
  const [reason, setReason] = useState("");
  const [points, setPoints] = useState(10);
  const [customers, setCustomers] = useState<{id: number, name: string, email: string}[]>([]);
  const [loading, setLoading] = useState(false);
  const {show} = useGlobalDialog();

  useEffect(() => {
    const token = typeof window !== 'undefined' ? localStorage.getItem('adminToken') : '';
    apiClient.get('/admin/users?userType=CUSTOMER&page=0&size=1000',
      { headers: { 'Authorization': `Bearer ${token}` } })
      .then(res => setCustomers(res.data.data.content || []));
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    const token = typeof window !== 'undefined' ? localStorage.getItem('adminToken') : '';
    try {
      await apiClient.post('/admin/penalties', {
        customerId: Number(customerId),
        reason,
        points,
        description: reason
      }, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        }
      });
      show('패널티가 등록되었습니다.');
      router.push('/admin/member-management/penalty');
    } catch (err) {
      const error = err as any;
      show('등록 실패: ' + (error?.response?.data?.message || error?.message || '알 수 없는 오류'));
    } finally {
      setLoading(false);
    }
  };

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
              <h1 className="text-3xl font-bold text-gray-900">패널티 등록</h1>
              <p className="text-gray-600 mt-1">사용자에게 패널티를 등록합니다</p>
            </div>
          </div>
        </div>

        {/* 폼 */}
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-8">
          <form onSubmit={handleSubmit} className="space-y-6">
            {/* 고객 선택 */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                고객 선택
              </label>
              <div className="relative">
                <User className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
                <select
                  value={customerId}
                  onChange={e => setCustomerId(e.target.value)}
                  className="w-full pl-10 pr-10 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-red-500 focus:border-transparent appearance-none bg-white transition-all"
                  required
                >
                  <option value="">고객을 선택하세요</option>
                  {customers.map(c => (
                    <option key={c.id} value={c.id}>{c.name} ({c.email})</option>
                  ))}
                </select>
                <div className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5">
                  <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                  </svg>
                </div>
              </div>
            </div>

            {/* 패널티 사유 */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                패널티 사유
              </label>
              <div className="relative">
                <FileText className="absolute left-3 top-3 text-gray-400 w-5 h-5" />
                <textarea
                  placeholder="패널티 사유를 입력하세요..."
                  value={reason}
                  onChange={e => setReason(e.target.value)}
                  className="w-full pl-10 pr-4 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-red-500 focus:border-transparent transition-all resize-none"
                  rows={4}
                  required
                />
              </div>
            </div>

            {/* 패널티 포인트 */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                패널티 포인트
              </label>
              <div className="relative">
                <Hash className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
                <input
                  type="number"
                  placeholder="패널티 포인트"
                  value={points}
                  onChange={e => setPoints(Number(e.target.value))}
                  className="w-full pl-10 pr-4 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-red-500 focus:border-transparent transition-all"
                  min="1"
                  max="100"
                  required
                />
              </div>
              <p className="mt-1 text-sm text-gray-500">1-100 사이의 값을 입력하세요</p>
            </div>

            {/* 등록 버튼 */}
            <div className="pt-6">
              <button
                type="submit"
                disabled={loading}
                className="w-full bg-red-600 text-white rounded-xl px-6 py-3 hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-red-500 focus:ring-offset-2 transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
              >
                {loading ? (
                  <>
                    <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-white"></div>
                    등록 중...
                  </>
                ) : (
                  <>
                    <Save className="w-5 h-5" />
                    패널티 등록
                  </>
                )}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
} 