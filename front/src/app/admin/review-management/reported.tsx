"use client";
import React, { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { getAdminReviewReportList, processAdminReviewReport } from "@/service/admin/reviewService";
import { AdminReviewReport, AdminReviewReportListRequest, ReviewReportStatus } from "@/types/admin/review";
import { useAdminAuthStore } from "@/store/admin/useAdminAuthStore";

export default function ReviewReportedPage() {
  const router = useRouter();
  const { accessToken } = useAdminAuthStore();
  const [reports, setReports] = useState<AdminReviewReport[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [statusFilter, setStatusFilter] = useState<ReviewReportStatus>('PENDING');

  // 로그인 체크
  useEffect(() => {
    if (!accessToken) {
      router.replace('/admin/login');
    }
  }, [accessToken, router]);

  // 리뷰 신고 목록 조회
  const fetchReports = async (page: number = 1) => {
    try {
      setLoading(true);
      setError(null);
      
      const params: AdminReviewReportListRequest = {
        page: page - 1,
        size: 10,
        status: statusFilter
      };

      const response = await getAdminReviewReportList(params);
      setReports(response.content);
      setTotalPages(response.totalPages);
      setCurrentPage(page);
    } catch (err: any) {
      console.error('리뷰 신고 목록 조회 실패:', err);
      setError(err.message || '리뷰 신고 목록을 불러오는데 실패했습니다.');
      if (err.response?.status === 403) {
        router.replace('/admin/login');
      }
    } finally {
      setLoading(false);
    }
  };

  // 초기 로드
  useEffect(() => {
    if (accessToken) {
      fetchReports();
    }
  }, [statusFilter, accessToken]);

  // 신고 처리
  const handleProcessReport = async (reportId: number, newStatus: ReviewReportStatus) => {
    try {
      await processAdminReviewReport(reportId, { newStatus });
      // 처리 후 목록 새로고침
      fetchReports(currentPage);
    } catch (err: any) {
      console.error('신고 처리 실패:', err);
      if (err.response?.status === 403) {
        router.replace('/admin/login');
        return;
      }
      alert(err.message || '신고 처리에 실패했습니다.');
    }
  };

  // 상태 텍스트 변환
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

  // 상태별 스타일
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

  if (!accessToken) {
    return null;
  }

  if (loading) {
    return (
      <div className="p-8">
        <div className="text-center">로딩 중...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="p-8">
        <div className="text-red-600 text-center mb-4">{error}</div>
        <button 
          onClick={() => fetchReports()} 
          className="bg-blue-500 text-white px-4 py-2 rounded"
        >
          다시 시도
        </button>
      </div>
    );
  }

  return (
    <div className="p-8">
      <h1 className="text-2xl font-bold mb-6">리뷰 신고 관리</h1>
      
      {/* 상태 필터 */}
      <div className="mb-6 flex gap-4 items-center">
        <select
          value={statusFilter}
          onChange={e => {
            const value = e.target.value as ReviewReportStatus;
            setStatusFilter(value);
          }}
          className="border rounded px-3 py-2"
        >
          <option value="PENDING">접수됨</option>
          <option value="UNDER_REVIEW">검토 중</option>
          <option value="RESOLVED_KEPT">리뷰 유지</option>
          <option value="RESOLVED_HIDDEN">리뷰 숨김</option>
          <option value="RESOLVED_REJECTED">신고 기각</option>
          <option value="REPORTER_ACCOUNT_INACTIVE">신고자 계정 비활성</option>
        </select>
      </div>

      {/* 신고 테이블 */}
      <div className="overflow-x-auto">
      <table className="min-w-full border text-sm">
        <thead>
          <tr className="bg-gray-100">
              <th className="px-4 py-2 border">상품명</th>
              <th className="px-4 py-2 border">리뷰 작성자</th>
              <th className="px-4 py-2 border">신고자</th>
              <th className="px-4 py-2 border">신고 사유</th>
              <th className="px-4 py-2 border">신고일</th>
              <th className="px-4 py-2 border">상태</th>
              <th className="px-4 py-2 border">처리</th>
          </tr>
        </thead>
        <tbody>
            {reports?.map(report => (
              <tr key={report.id} className="hover:bg-gray-50">
                <td className="px-4 py-2 border">{report.review.productName}</td>
                <td className="px-4 py-2 border">{report.review.customerName}</td>
                <td className="px-4 py-2 border">{report.reporterName}</td>
                <td className="px-4 py-2 border max-w-xs truncate" title={report.reason}>
                  {report.reason}
                </td>
                <td className="px-4 py-2 border">
                  {new Date(report.createdAt).toLocaleDateString()}
                </td>
                <td className={`px-4 py-2 border text-center ${getStatusStyle(report.status)}`}>
                  {getStatusText(report.status)}
                </td>
                <td className="px-4 py-2 border text-center">
                  {report.status === 'PENDING' && (
                    <div className="flex gap-2 justify-center">
                      <button 
                        className="bg-blue-500 text-white px-2 py-1 rounded text-xs hover:bg-blue-600"
                        onClick={() => handleProcessReport(report.id, 'UNDER_REVIEW')}
                      >
                        검토 시작
                      </button>
                    </div>
                  )}
                  {report.status === 'UNDER_REVIEW' && (
                    <div className="flex gap-2 justify-center">
                      <button 
                        className="bg-green-500 text-white px-2 py-1 rounded text-xs hover:bg-green-600"
                        onClick={() => handleProcessReport(report.id, 'RESOLVED_KEPT')}
                      >
                        리뷰 유지
                      </button>
                      <button 
                        className="bg-red-500 text-white px-2 py-1 rounded text-xs hover:bg-red-600"
                        onClick={() => handleProcessReport(report.id, 'RESOLVED_HIDDEN')}
                      >
                        리뷰 숨김
                      </button>
                      <button 
                        className="bg-gray-500 text-white px-2 py-1 rounded text-xs hover:bg-gray-600"
                        onClick={() => handleProcessReport(report.id, 'RESOLVED_REJECTED')}
                      >
                        신고 기각
                      </button>
                    </div>
                  )}
                  {!['PENDING', 'UNDER_REVIEW'].includes(report.status) && (
                    <span className="text-gray-500">처리완료</span>
                  )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      </div>

      {/* 페이지네이션 */}
      {totalPages > 1 && (
        <div className="mt-6 flex justify-center gap-2">
          <button
            onClick={() => fetchReports(currentPage - 1)}
            disabled={currentPage === 1}
            className="px-3 py-1 border rounded disabled:opacity-50"
          >
            이전
          </button>
          <span className="px-3 py-1">
            {currentPage} / {totalPages}
          </span>
          <button
            onClick={() => fetchReports(currentPage + 1)}
            disabled={currentPage === totalPages}
            className="px-3 py-1 border rounded disabled:opacity-50"
          >
            다음
          </button>
        </div>
      )}

      {(!reports || reports.length === 0) && !loading && (
        <div className="text-center text-gray-500 mt-8">
          조회된 신고가 없습니다.
        </div>
      )}
    </div>
  );
} 