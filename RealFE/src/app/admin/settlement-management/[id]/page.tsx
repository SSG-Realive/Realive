"use client";
import { useParams, useRouter } from "next/navigation";
import { useState, useEffect } from 'react';
import { Button } from "@/components/ui/button";
import { adminSettlementService } from "@/service/admin/settlementService";
import { AdminPayoutDetailResponse } from "@/types/admin/settlement";

export default function SettlementDetailPage() {
  const params = useParams();
  const router = useRouter();
  const { id } = params;
  const payoutId = Number(id);

  const [settlement, setSettlement] = useState<AdminPayoutDetailResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // 인증 체크
  useEffect(() => {
    if (typeof window !== 'undefined' && !localStorage.getItem('adminToken')) {
      router.replace('/admin/login');
      return;
    }
  }, [router]);

  // 정산 상세 데이터 로드
  useEffect(() => {
    const loadSettlementDetail = async () => {
      if (!payoutId || isNaN(payoutId)) {
        setError('유효하지 않은 정산 ID입니다.');
        setLoading(false);
        return;
      }

      try {
        setLoading(true);
        setError(null);
        const data = await adminSettlementService.getPayoutDetail(payoutId);
        setSettlement(data);
      } catch (err) {
        console.error('정산 상세 데이터 로드 실패:', err);
        setError('정산 상세 정보를 불러오는데 실패했습니다.');
      } finally {
        setLoading(false);
      }
    };

    loadSettlementDetail();
  }, [payoutId]);

  if (typeof window !== 'undefined' && !localStorage.getItem('adminToken')) {
    return null;
  }

  if (loading) {
    return (
      <div className="w-full max-w-full min-h-screen bg-gray-50 p-2 sm:p-6 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-green-600 mx-auto mb-4"></div>
          <p className="text-gray-600">정산 상세 정보를 불러오는 중...</p>
        </div>
      </div>
    );
  }

  if (error || !settlement) {
    return (
      <div className="w-full max-w-full min-h-screen bg-gray-50 p-2 sm:p-6 flex items-center justify-center">
        <div className="text-center">
          <div className="text-red-500 text-6xl mb-4">⚠️</div>
          <h3 className="text-lg font-medium text-gray-900 mb-2">오류가 발생했습니다</h3>
          <p className="text-gray-600 mb-4">{error || '정산 정보를 찾을 수 없습니다.'}</p>
          <Button onClick={() => router.push('/admin/settlement-management')} variant="default">
            목록으로 돌아가기
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div className="w-full max-w-full min-h-screen bg-gray-50 p-2 sm:p-6 overflow-x-auto">
      <div className="w-full max-w-6xl mx-auto">
        {/* 헤더 */}
        <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
          <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
            <div>
              <h1 className="text-2xl font-bold text-gray-900">정산 상세</h1>
              <p className="text-sm text-gray-600 mt-1">정산 ID: {settlement.payoutId}</p>
            </div>
            <Button 
              onClick={() => router.push('/admin/settlement-management')}
              variant="outline"
            >
              목록으로 돌아가기
            </Button>
          </div>
        </div>

        {/* 정산 기본 정보 */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
          {/* 판매자 정보 */}
          <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">판매자 정보</h2>
            <div className="space-y-3">
              <div>
                <label className="text-sm font-medium text-gray-500">판매자명</label>
                <p className="text-gray-900">{settlement.sellerName}</p>
              </div>
              <div>
                <label className="text-sm font-medium text-gray-500">이메일</label>
                <p className="text-gray-900">{settlement.sellerEmail}</p>
              </div>
              <div>
                <label className="text-sm font-medium text-gray-500">판매자 ID</label>
                <p className="text-gray-900">{settlement.sellerId}</p>
              </div>
            </div>
          </div>

          {/* 정산 요약 */}
          <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">정산 요약</h2>
            <div className="space-y-3">
              <div>
                <label className="text-sm font-medium text-gray-500">정산 기간</label>
                <p className="text-gray-900">{settlement.periodStart} ~ {settlement.periodEnd}</p>
              </div>
              <div>
                <label className="text-sm font-medium text-gray-500">총 판매액</label>
                <p className="text-gray-900 font-semibold">{settlement.totalSales.toLocaleString()}원</p>
              </div>
              <div>
                <label className="text-sm font-medium text-gray-500">수수료</label>
                <p className="text-gray-900">{settlement.totalCommission.toLocaleString()}원</p>
              </div>
              <div>
                <label className="text-sm font-medium text-gray-500">정산 금액</label>
                <p className="text-green-600 font-bold text-lg">{settlement.payoutAmount.toLocaleString()}원</p>
              </div>
              <div>
                <label className="text-sm font-medium text-gray-500">처리일</label>
                <p className="text-gray-900">{new Date(settlement.processedAt).toLocaleString()}</p>
              </div>
            </div>
          </div>
        </div>

        {/* 판매 상세 내역 */}
        <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">판매 상세 내역</h2>
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    판매 로그 ID
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    주문 아이템 ID
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    상품 ID
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    수량
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    단가
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    총액
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    판매일
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {settlement.salesDetails.map((sale) => (
                  <tr key={sale.salesLogId} className="hover:bg-gray-50">
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {sale.salesLogId}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {sale.orderItemId}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {sale.productId}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {sale.quantity}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {sale.unitPrice.toLocaleString()}원
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 font-medium">
                      {sale.totalPrice.toLocaleString()}원
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {new Date(sale.soldAt).toLocaleDateString()}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        {/* 수수료 상세 내역 */}
        <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">수수료 상세 내역</h2>
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    수수료 로그 ID
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    판매 로그 ID
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    수수료율
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    수수료 금액
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    기록일
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {settlement.commissionDetails.map((commission) => (
                  <tr key={commission.commissionLogId} className="hover:bg-gray-50">
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {commission.commissionLogId}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {commission.salesLogId}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {commission.commissionRate}%
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 font-medium">
                      {commission.commissionAmount.toLocaleString()}원
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {new Date(commission.recordedAt).toLocaleDateString()}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
} 