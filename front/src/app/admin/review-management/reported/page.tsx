"use client";
import { useRouter } from "next/navigation";
import { useState, useEffect } from "react";
import { getAdminReviewReportList, processAdminReviewReport } from "@/service/admin/reviewService";
import { AdminReviewReport, ReviewReportStatus, AdminReviewReportListRequest } from "@/types/admin/review";
import { useAdminAuthStore } from "@/store/admin/useAdminAuthStore";

export default function ReviewReportedPage() {
  const router = useRouter();
  const { accessToken } = useAdminAuthStore();
  const [reports, setReports] = useState<AdminReviewReport[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [statusFilter, setStatusFilter] = useState<ReviewReportStatus>('');

  useEffect(() => {
    if (!accessToken) {
      router.replace('/admin/login');
    }
  }, [accessToken, router]);

  const fetchReports = async (page: number = 1) => {
    if (!accessToken) return;
    try {
      setLoading(true);
      setError(null);
      const params: AdminReviewReportListRequest = {
        page: page - 1,
        size: 10,
        status: statusFilter,
        sort: 'createdAt,desc',
      };
      console.log('리뷰 신고 목록 조회 요청:', params);
      const response = await getAdminReviewReportList(params);
      console.log('리뷰 신고 목록 조회 응답:', response);
      setReports(response.content || []);
      setTotalPages(response.totalPages);
      setCurrentPage(page);
    } catch (err: any) {
      console.error("리뷰 신고 목록 조회 실패:", err);
      console.error('에러 상세 정보:', {
        message: err.message,
        status: err.response?.status,
        statusText: err.response?.statusText,
        data: err.response?.data,
        headers: err.response?.headers
      });
      setError(err.message || "리뷰 신고 목록을 불러오는데 실패했습니다.");
      if (err.response?.status === 403) {
        router.replace('/admin/login');
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (accessToken) {
      console.log('리뷰 신고 페이지 - accessToken 확인:', accessToken ? '있음' : '없음');
      fetchReports(currentPage);
    } else {
      console.log('리뷰 신고 페이지 - accessToken 없음, 로그인 페이지로 이동');
    }
  }, [statusFilter, accessToken, currentPage]);

  const handleProcessReport = async (reportId: number, newStatus: ReviewReportStatus) => {
    try {
      await processAdminReviewReport(reportId, { newStatus });
      fetchReports(currentPage);
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

  const getStatusStyle = (status: ReviewReportStatus) => {
    switch (status) {
      case 'PENDING': return 'text-yellow-600';
      case 'UNDER_REVIEW': return 'text-blue-600';
      case 'RESOLVED_KEPT': return 'text-green-600';
      case 'RESOLVED_HIDDEN': return 'text-red-600';
      case 'RESOLVED_REJECTED': return 'text-gray-600';
      case 'REPORTER_ACCOUNT_INACTIVE': return 'text-red-800';
      default: return '';
    }
  };

  if (loading && !reports.length) {
    return <div className="p-8 text-center">로딩 중...</div>;
  }

  if (error) {
    return <div className="p-8 text-center text-red-500">{error}</div>;
  }

  return (
    <div className="p-8">
      <h1 className="text-2xl font-bold mb-6">리뷰 신고 관리</h1>
      <div className="mb-4">
        <select
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value as ReviewReportStatus)}
          className="border rounded px-3 py-2"
        >
          <option value="">전체</option>
          <option value="PENDING">접수됨</option>
          <option value="RESOLVED_KEPT">리뷰 유지</option>
          <option value="RESOLVED_HIDDEN">리뷰 숨김</option>
          <option value="REPORTER_ACCOUNT_INACTIVE">신고자 계정 비활성</option>
        </select>
      </div>
      <table className="min-w-full border text-sm">
        <thead>
          <tr className="bg-gray-100">
            <th className="px-4 py-2 border">신고자</th>
            <th className="px-4 py-2 border">신고 사유</th>
            <th className="px-4 py-2 border">신고일</th>
            <th className="px-4 py-2 border">상태</th>
            <th className="px-4 py-2 border">처리</th>
          </tr>
        </thead>
        <tbody>
          {reports.map((report) => (
            <tr key={report.reportId}>
              <td className="px-4 py-2 border">{report.reporterName}</td>
              <td className="px-4 py-2 border">{report.reason}</td>
              <td className="px-4 py-2 border">{new Date(report.reportedAt).toLocaleDateString()}</td>
              <td className={`px-4 py-2 border font-semibold ${getStatusStyle(report.status)}`}>{getStatusText(report.status)}</td>
              <td className="px-4 py-2 border">
                <div className="flex items-center justify-center gap-2">
                  <button
                    className="text-blue-600 underline"
                    onClick={() => {
                      console.log('신고 상세 버튼 클릭:', report.reportId);
                      try {
                        router.push(`/admin/review-management/reported/${report.reportId}`);
                      } catch (error) {
                        console.error('라우터 에러:', error);
                        window.location.href = `/admin/review-management/reported/${report.reportId}`;
                      }
                    }}
                  >
                    상세
                  </button>
                  {report.status === 'PENDING' && (
                    <>
                      <button onClick={() => handleProcessReport(report.reportId, 'RESOLVED_KEPT')} className="bg-green-500 text-white px-2 py-1 rounded">유지</button>
                      <button onClick={() => handleProcessReport(report.reportId, 'RESOLVED_HIDDEN')} className="bg-red-500 text-white px-2 py-1 rounded">숨김</button>
                    </>
                  )}
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      <div className="mt-6 flex justify-center gap-2">
        <button onClick={() => fetchReports(currentPage - 1)} disabled={currentPage === 1}>이전</button>
        <span>{currentPage} / {totalPages}</span>
        <button onClick={() => fetchReports(currentPage + 1)} disabled={currentPage === totalPages}>다음</button>
      </div>
    </div>
  );
} 