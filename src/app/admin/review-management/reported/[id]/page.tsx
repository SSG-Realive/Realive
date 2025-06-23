"use client";
import { useParams, useRouter } from "next/navigation";
import { useState, useEffect } from "react";
import { getTrafficLightEmoji, getTrafficLightText, getTrafficLightBgClass } from "@/types/admin/review";
import { getAdminReviewReport, processAdminReviewReport } from "@/service/admin/reviewService";
import { AdminReviewReport, ReviewReportStatus } from "@/types/admin/review";
import { useAdminAuthStore } from "@/store/admin/useAdminAuthStore";

export default function ReportedReviewDetailPage() {
  const params = useParams();
  const router = useRouter();
  const { accessToken } = useAdminAuthStore();
  const { id } = params;
  
  const [report, setReport] = useState<AdminReviewReport | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const reportId = Number(id);

  const fetchReportDetail = async () => {
    if (!accessToken || isNaN(reportId)) return;
    try {
      setLoading(true);
      setError(null);
      const data = await getAdminReviewReport(reportId);
      setReport(data);
    } catch (err: any) {
      console.error("신고 상세 조회 실패:", err);
      setError(err.message || "신고 정보를 불러오는데 실패했습니다.");
      if (err.response?.status === 403) {
        router.replace('/admin/login');
      }
    } finally {
      setLoading(false);
    }
  };
  
  useEffect(() => {
    fetchReportDetail();
  }, [accessToken, id]);

  const handleProcessReport = async (newStatus: ReviewReportStatus) => {
    if (!report) return;
    try {
      await processAdminReviewReport(report.reportId, { newStatus });
      fetchReportDetail(); // Refresh data
    } catch (err: any) {
      if (err.response?.status === 403) {
        router.replace('/admin/login');
        return;
      }
      alert(err.message || "신고 처리에 실패했습니다.");
    }
  };

  const getStatusText = (status: ReviewReportStatus) => {
    switch (status) {
      case 'PENDING': return '접수됨';
      case 'UNDER_REVIEW': return '검토 중';
      case 'RESOLVED_KEPT': return '리뷰 유지';
      case 'RESOLVED_HIDDEN': return '리뷰 숨김';
      case 'RESOLVED_REJECTED': return '신고 기각';
      case 'REPORTER_ACCOUNT_INACTIVE': return '신고자 계정 비활성';
      default: return status;
    }
  };

  if (loading) return <div className="p-8 text-center">로딩 중...</div>;
  if (error) return <div className="p-8 text-center text-red-500">{error}</div>;
  if (!report) {
    return <div className="p-8">신고된 리뷰 정보를 찾을 수 없습니다.</div>;
  }

  return (
    <div className="p-8 max-w-4xl mx-auto">
      <h2 className="text-2xl font-bold mb-6">신고된 리뷰 상세</h2>
      <div className="bg-white rounded-lg p-6 shadow-md space-y-8">
        {/* Reviewer and Reporter Info */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
          {/* Reviewer */}
          <div>
            <h3 className="text-lg font-semibold border-b pb-2 mb-4">리뷰 작성자 정보</h3>
            <div className="flex items-center gap-4">
              <img src={report.customerImage || '/images/placeholder.png'} alt={report.customerName} className="w-16 h-16 rounded-full border" />
              <div>
                <p className="text-xl font-bold">{report.customerName}</p>
                <p className="text-sm text-gray-500">고객 ID: {report.customerId}</p>
              </div>
            </div>
          </div>
          {/* Reporter */}
          <div>
            <h3 className="text-lg font-semibold border-b pb-2 mb-4">신고자 정보</h3>
            <div className="flex items-center gap-4">
               {/* Reporter image not available in API */}
              <div>
                <p className="text-xl font-bold">{report.reporterName}</p>
                <p className="text-sm text-gray-500">신고자 ID: {report.reporterId}</p>
              </div>
            </div>
          </div>
        </div>

        {/* Report Details */}
        <div>
          <h3 className="text-lg font-semibold border-b pb-2 mb-4">신고 내용</h3>
          <div className="space-y-4">
            <p><span className="font-semibold">상품명:</span> {report.productName}</p>
            <p><span className="font-semibold">신고 사유:</span> {report.reason}</p>
            <p><span className="font-semibold">리뷰 원문:</span></p>
            <blockquote className="border-l-4 pl-4 text-gray-700 italic">{report.content}</blockquote>
            <p><span className="font-semibold">평점:</span> 
              <div className={`inline-flex items-center space-x-2 mt-1 px-3 py-1 rounded-full border ${getTrafficLightBgClass(report.rating)}`}>
                <span className="text-xl">{getTrafficLightEmoji(report.rating)}</span>
                <span className="text-sm font-medium">{getTrafficLightText(report.rating)}</span>
              </div>
            </p>
          </div>
        </div>
        
        {/* Status and Actions */}
        <div className="border-t pt-6">
          <div className="flex justify-between items-center">
            <div>
              <p className="font-semibold">현재 상태</p>
              <p className="text-lg">{getStatusText(report.status)}</p>
            </div>
            <div className="flex gap-2">
              {report.status === 'PENDING' && (
                 <button onClick={() => handleProcessReport('UNDER_REVIEW')} className="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded">검토 시작</button>
              )}
              {report.status === 'UNDER_REVIEW' && (
                <>
                  <button onClick={() => handleProcessReport('RESOLVED_KEPT')} className="bg-green-500 hover:bg-green-600 text-white px-4 py-2 rounded">리뷰 유지</button>
                  <button onClick={() => handleProcessReport('RESOLVED_HIDDEN')} className="bg-red-500 hover:bg-red-600 text-white px-4 py-2 rounded">리뷰 숨김</button>
                  <button onClick={() => handleProcessReport('RESOLVED_REJECTED')} className="bg-gray-500 hover:bg-gray-600 text-white px-4 py-2 rounded">신고 기각</button>
                </>
              )}
              {report.status === 'RESOLVED_KEPT' && (
                <button onClick={() => handleProcessReport('RESOLVED_HIDDEN')} className="bg-red-500 hover:bg-red-600 text-white px-4 py-2 rounded">리뷰 숨김으로 변경</button>
              )}
              {report.status === 'RESOLVED_HIDDEN' && (
                <button onClick={() => handleProcessReport('RESOLVED_KEPT')} className="bg-green-500 hover:bg-green-600 text-white px-4 py-2 rounded">리뷰 유지로 변경</button>
              )}
            </div>
          </div>
        </div>

        <div className="mt-6 flex justify-end gap-2">
          <button 
            className="px-4 py-2 bg-gray-200 rounded hover:bg-gray-300"
            onClick={() => router.back()}
          >
            목록으로
          </button>
        </div>
      </div>
    </div>
  );
} 