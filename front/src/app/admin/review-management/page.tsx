"use client";
import React, { useState, useEffect } from "react";
import Link from "next/link";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { ScrollArea, ScrollBar } from "@/components/ui/scroll-area";
import { useRouter } from "next/navigation";
import { getAdminReviewList, getAdminReviewReportList, getAdminReviewQnaList } from "@/service/admin/reviewService";
import { AdminReview, AdminReviewReport, AdminReviewQna, ReviewReportStatus } from "@/types/admin/review";
import { useAdminAuthStore } from "@/store/admin/useAdminAuthStore";

export default function ReviewManagementPage() {
  const router = useRouter();
  const { accessToken } = useAdminAuthStore();
  const [reviews, setReviews] = useState<AdminReview[]>([]);
  const [reports, setReports] = useState<AdminReviewReport[]>([]);
  const [qnas, setQnas] = useState<AdminReviewQna[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // 데이터 조회
  const fetchData = async () => {
    try {
      setLoading(true);
      setError(null);

      // 리뷰 목록 (최신 5개)
      const reviewResponse = await getAdminReviewList({
        page: 0,
        size: 5,
        sort: 'createdAt,desc',
      });

      // 신고 목록 (최신 5개)
      const reportResponse = await getAdminReviewReportList({
        page: 0,
        size: 5,
        status: 'PENDING',
        sort: 'createdAt,desc'
      });

      // Q&A 목록 (최신 5개)
      const qnaResponse = await getAdminReviewQnaList({
        page: 0,
        size: 5
      });

      setReviews(reviewResponse.content);
      setReports(reportResponse.content);
      setQnas(qnaResponse.content);
    } catch (err: any) {
      console.error('데이터 조회 실패:', err);
      setError(err.message || '데이터를 불러오는데 실패했습니다.');
      if (err.response?.status === 403) {
        router.replace('/admin/login');
      }
    } finally {
      setLoading(false);
    }
  };

  // 로그인 체크
  useEffect(() => {
    if (!accessToken) {
      router.replace('/admin/login');
    }
  }, [accessToken, router]);

  // 초기 로드
  useEffect(() => {
    if (accessToken) {
      fetchData();
    }
  }, [accessToken]);

  // 상태 텍스트 변환
  const getReviewStatusText = (isHidden: boolean) => {
    return isHidden ? '숨김' : '공개';
  };

  const getReportStatusText = (status: ReviewReportStatus) => {
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

  const getQnaStatusText = (isAnswered: boolean) => {
    return isAnswered ? '답변완료' : '미답변';
  };

  // 상태별 스타일
  const getReviewStatusStyle = (isHidden: boolean) => {
    return isHidden ? 'text-gray-600' : 'text-green-600';
  };

  const getReportStatusStyle = (status: ReviewReportStatus) => {
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

  const getQnaStatusStyle = (isAnswered: boolean) => {
    return isAnswered ? 'text-green-600' : 'text-yellow-600';
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
          onClick={() => fetchData()} 
          className="bg-blue-500 text-white px-4 py-2 rounded"
        >
          다시 시도
        </button>
      </div>
    );
  }

  return (
    <div className="space-y-8">
      <div className="grid grid-cols-1 lg:grid-cols-2 xl:grid-cols-3 gap-8">
        {/* 리뷰 목록 요약 */}
        <div className="bg-white rounded-2xl shadow-lg p-6 hover:shadow-xl transition-shadow">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-lg font-bold text-gray-800">최신 리뷰</h2>
            <Link href="/admin/review-management/list" className="text-sm font-semibold text-blue-600 hover:text-blue-800">
              전체보기 →
            </Link>
          </div>
          <table className="w-full text-sm text-left">
            <thead className="text-xs text-gray-700 uppercase bg-gray-50">
              <tr>
                <th scope="col" className="px-4 py-2">상품</th>
                <th scope="col" className="px-4 py-2">작성자</th>
                <th scope="col" className="px-4 py-2">상태</th>
              </tr>
            </thead>
            <tbody>
              {reviews?.slice(0, 5).map(review => (
                <tr key={review.reviewId} onClick={() => router.push(`/admin/review-management/list/${review.reviewId}`)} className="bg-white border-b hover:bg-gray-50 cursor-pointer">
                  <td className="px-4 py-2 font-medium text-gray-900 truncate">{review.productName || '상품 정보 없음'}</td>
                  <td className="px-4 py-2">{review.customerName}</td>
                  <td className={`px-4 py-2 font-semibold ${getReviewStatusStyle(review.isHidden)}`}>
                    {getReviewStatusText(review.isHidden)}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* 리뷰 신고 관리 요약 */}
        <div className="bg-white rounded-2xl shadow-lg p-6 hover:shadow-xl transition-shadow">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-lg font-bold text-gray-800">리뷰 신고 관리</h2>
            <Link href="/admin/review-management/reported" className="text-sm font-semibold text-blue-600 hover:text-blue-800">
              전체보기 →
            </Link>
          </div>
          <table className="w-full text-sm text-left">
            <thead className="text-xs text-gray-700 uppercase bg-gray-50">
              <tr>
                <th scope="col" className="px-4 py-2">작성자</th>
                <th scope="col" className="px-4 py-2">사유</th>
                <th scope="col" className="px-4 py-2">상태</th>
              </tr>
            </thead>
            <tbody>
              {reports?.slice(0, 5).map(report => (
                <tr key={report.reportId} onClick={() => router.push(`/admin/review-management/reported/${report.reportId}`)} className="bg-white border-b hover:bg-gray-50 cursor-pointer">
                  <td className="px-4 py-2">{report.reporterName}</td>
                  <td className="px-4 py-2 truncate" title={report.reason}>{report.reason}</td>
                  <td className={`px-4 py-2 font-semibold ${getReportStatusStyle(report.status)}`}>
                    {getReportStatusText(report.status)}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* Q&A 관리 요약 */}
        <div className="bg-white rounded-2xl shadow-lg p-6 hover:shadow-xl transition-shadow">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-lg font-bold text-gray-800">Q&A 관리</h2>
            <Link href="/admin/review-management/qna" className="text-sm font-semibold text-blue-600 hover:text-blue-800">
              전체보기 →
            </Link>
          </div>
          <table className="w-full text-sm text-left">
            <thead className="text-xs text-gray-700 uppercase bg-gray-50">
              <tr>
                <th scope="col" className="px-4 py-2">제목</th>
                <th scope="col" className="px-4 py-2">상태</th>
              </tr>
            </thead>
            <tbody>
              {qnas?.slice(0, 5).map(qna => (
                <tr key={qna.id} onClick={() => router.push(`/admin/review-management/qna/${qna.id}`)} className="bg-white border-b hover:bg-gray-50 cursor-pointer">
                  <td className="px-4 py-2 font-medium text-gray-900 truncate" title={qna.title}>{qna.title}</td>
                  <td className={`px-4 py-2 font-semibold ${getQnaStatusStyle(qna.isAnswered)}`}>
                    {getQnaStatusText(qna.isAnswered)}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* 통계 정보 */}
      <div className="pt-4">
        <h2 className="text-xl font-bold mb-4 text-gray-800">리뷰 관리 통계</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-6">
          <Card className="rounded-2xl shadow-lg hover:shadow-xl transition-shadow">
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">전체 리뷰</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{reviews.length}</div>
            </CardContent>
          </Card>
          <Card className="rounded-2xl shadow-lg hover:shadow-xl transition-shadow">
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">신고된 리뷰</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{reports.length}</div>
            </CardContent>
          </Card>
          <Card className="rounded-2xl shadow-lg hover:shadow-xl transition-shadow">
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">미답변 Q&A</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold text-yellow-600">{qnas.filter(q => !q.isAnswered).length}</div>
            </CardContent>
          </Card>
          <Card className="rounded-2xl shadow-lg hover:shadow-xl transition-shadow">
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">숨겨진 리뷰</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{reviews.filter(r => r.isHidden).length}</div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
} 