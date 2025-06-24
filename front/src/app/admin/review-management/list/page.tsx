"use client";
import React, { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { getAdminReviewList, updateAdminReview } from "@/service/admin/reviewService";
import { AdminReview, AdminReviewListRequest, AdminReviewListResponse, getTrafficLightEmoji, getTrafficLightText, getTrafficLightBgClass } from "@/types/admin/review";
import { useAdminAuthStore } from "@/store/admin/useAdminAuthStore";

export default function ReviewListPage() {
  const router = useRouter();
  const { accessToken } = useAdminAuthStore();
  const [reviews, setReviews] = useState<AdminReview[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [sortOption, setSortOption] = useState('createdAt,desc');
  const [filters, setFilters] = useState({
    productFilter: '',
    sellerFilter: '',
  });

  const handleFilterChange = (key: string, value: string) => {
    setFilters(prev => ({ ...prev, [key]: value }));
  };

  const applyFilters = () => {
    fetchReviews(1); // Reset to first page
  };

  const fetchReviews = async (page: number = 1) => {
    try {
      setLoading(true);
      setError(null);
      
      const params: AdminReviewListRequest = {
        page: page - 1,
        size: 10,
        sort: sortOption,
        productFilter: filters.productFilter || undefined,
        sellerFilter: filters.sellerFilter || undefined,
      };

      console.log('리뷰 목록 조회 요청:', params);
      const response = await getAdminReviewList(params);
      console.log('리뷰 목록 조회 응답:', response);
      setReviews(response.content);
      setTotalPages(response.totalPages);
      setCurrentPage(page);
    } catch (err: any) {
      console.error('리뷰 목록 조회 실패:', err);
      console.error('에러 상세 정보:', {
        message: err.message,
        status: err.response?.status,
        statusText: err.response?.statusText,
        data: err.response?.data,
        headers: err.response?.headers
      });
      setError(err.message || '리뷰 목록을 불러오는데 실패했습니다.');
      if (err.response?.status === 403) {
        router.replace('/admin/login');
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (accessToken) {
      console.log('리뷰 목록 페이지 - accessToken 확인:', accessToken ? '있음' : '없음');
      fetchReviews(currentPage);
    } else {
      console.log('리뷰 목록 페이지 - accessToken 없음, 로그인 페이지로 이동');
    }
  }, [accessToken, currentPage, sortOption]);

  useEffect(() => {
    if (!accessToken) {
      router.replace('/admin/login');
    }
  }, [accessToken, router]);

  const handleToggleVisibility = async (reviewId: number, isHidden: boolean) => {
    try {
      await updateAdminReview(reviewId, !isHidden);
      // Refresh list
      setReviews(reviews.map(r => r.reviewId === reviewId ? { ...r, isHidden: !isHidden } : r));
    } catch (err: any) {
      console.error('리뷰 상태 변경 실패:', err);
      alert(err.message || '리뷰 상태 변경에 실패했습니다.');
    }
  };

  if (loading && !reviews.length) {
    return <div className="p-8 text-center">로딩 중...</div>;
  }

  if (error) {
    return (
      <div className="p-8 text-center text-red-500">
        <p>{error}</p>
        <button onClick={() => fetchReviews(1)} className="mt-4 bg-blue-500 text-white px-4 py-2 rounded">
          다시 시도
        </button>
      </div>
    );
  }

  return (
    <div className="w-full max-w-full min-h-screen bg-gray-50 p-2 sm:p-8 overflow-x-auto">
      <h1 className="text-2xl font-bold mb-6">리뷰 목록</h1>
      
      <div className="mb-6 flex gap-4 items-end">
        <input
          type="text"
          placeholder="상품명 검색"
          value={filters.productFilter}
          onChange={(e) => handleFilterChange('productFilter', e.target.value)}
          className="border rounded px-3 py-2"
        />
        <input
          type="text"
          placeholder="판매자명 검색"
          value={filters.sellerFilter}
          onChange={(e) => handleFilterChange('sellerFilter', e.target.value)}
          className="border rounded px-3 py-2"
        />
        <button
          onClick={applyFilters}
          className="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600"
        >
          검색
        </button>
      </div>

      <div className="mb-4 flex justify-end">
        <select
          value={sortOption}
          onChange={e => setSortOption(e.target.value)}
          className="border rounded px-3 py-2"
        >
          <option value="createdAt,desc">최신순</option>
          <option value="rating,desc">평점 높은 순</option>
          <option value="rating,asc">평점 낮은 순</option>
        </select>
      </div>

      {/* 데스크탑 표 */}
      <div className="hidden md:block">
        <div className="overflow-x-auto w-full">
          <table className="min-w-[900px] w-full border text-sm">
            <thead>
              <tr className="bg-gray-100">
                <th className="whitespace-nowrap px-2 py-2 text-xs">상품명</th>
                <th className="whitespace-nowrap px-2 py-2 text-xs">고객명</th>
                <th className="whitespace-nowrap px-2 py-2 text-xs">판매자명</th>
                <th className="whitespace-nowrap px-2 py-2 text-xs">내용</th>
                <th className="whitespace-nowrap px-2 py-2 text-xs">평점</th>
                <th className="whitespace-nowrap px-2 py-2 text-xs">작성일</th>
                <th className="whitespace-nowrap px-2 py-2 text-xs">상태</th>
                <th className="whitespace-nowrap px-2 py-2 text-xs">신고수</th>
                <th className="whitespace-nowrap px-2 py-2 text-xs">상세/처리</th>
              </tr>
            </thead>
            <tbody>
              {reviews?.map(review => (
                <tr key={review.reviewId} className="hover:bg-gray-50">
                  <td className="whitespace-nowrap px-2 py-2 text-xs">{review.productName || 'N/A'}</td>
                  <td className="whitespace-nowrap px-2 py-2 text-xs">{review.customerName}</td>
                  <td className="whitespace-nowrap px-2 py-2 text-xs">{review.sellerName}</td>
                  <td className="whitespace-nowrap px-2 py-2 text-xs max-w-xs truncate" title={review.content || review.contentSummary}>
                    {review.contentSummary || review.content}
                  </td>
                  <td className="whitespace-nowrap px-2 py-2 text-xs text-center">
                    <div className={`flex items-center justify-center space-x-2 px-3 py-1 rounded-full border ${getTrafficLightBgClass(review.rating)}`}>
                      <span className="text-lg">{getTrafficLightEmoji(review.rating)}</span>
                      <span className="text-xs font-medium">{getTrafficLightText(review.rating)}</span>
                    </div>
                  </td>
                  <td className="whitespace-nowrap px-2 py-2 text-xs">
                    {new Date(review.createdAt).toLocaleDateString()}
                  </td>
                  <td className="whitespace-nowrap px-2 py-2 text-xs text-center">
                    <button
                      onClick={() => handleToggleVisibility(review.reviewId, review.isHidden)}
                      className={`px-2 py-1 rounded text-xs text-white transition-colors ${
                        review.isHidden 
                          ? 'bg-gray-500 hover:bg-gray-600' 
                          : 'bg-green-500 hover:bg-green-600'
                      }`}
                    >
                      {review.isHidden ? '숨김' : '공개'}
                    </button>
                  </td>
                  <td className="whitespace-nowrap px-2 py-2 text-xs text-center">
                    {review.reportCount || 0}
                  </td>
                  <td className="whitespace-nowrap px-2 py-2 text-xs text-center">
                    <button 
                      className="text-blue-600 underline"
                      onClick={() => {
                        console.log('리뷰 상세 버튼 클릭:', review.reviewId);
                        try {
                          router.push(`/admin/review-management/list/${review.reviewId}`);
                        } catch (error) {
                          console.error('라우터 에러:', error);
                          window.location.href = `/admin/review-management/list/${review.reviewId}`;
                        }
                      }}
                    >
                      상세
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {totalPages > 1 && (
          <div className="mt-6 flex justify-center gap-2">
            <button
              onClick={() => fetchReviews(currentPage - 1)}
              disabled={currentPage === 1}
              className="px-3 py-1 border rounded disabled:opacity-50"
            >
              이전
            </button>
            <span className="px-3 py-1">
              {currentPage} / {totalPages}
            </span>
            <button
              onClick={() => fetchReviews(currentPage + 1)}
              disabled={currentPage === totalPages}
              className="px-3 py-1 border rounded disabled:opacity-50"
            >
              다음
            </button>
          </div>
        )}

        {(!reviews || reviews.length === 0) && !loading && (
          <div className="text-center text-gray-500 mt-8">
            조회된 리뷰가 없습니다.
          </div>
        )}
      </div>

      {/* 모바일 카드형 */}
      <div className="block md:hidden space-y-4">
        {reviews?.map((review, idx) => (
          <div key={review.reviewId} className="bg-white rounded shadow p-4">
            <div className="font-bold mb-2">상품명: {review.productName || 'N/A'}</div>
            <div className="mb-1">고객명: {review.customerName}</div>
            <div className="mb-1">판매자명: {review.sellerName}</div>
            <div className="mb-1">내용: {review.contentSummary || review.content}</div>
            <div className="mb-1">작성일: {new Date(review.createdAt).toLocaleDateString()}</div>
            <div className="mb-1">
              상태: {review.isHidden ? '숨김' : '공개'}
              <button
                className={`ml-2 px-3 py-1 rounded text-white text-xs ${review.isHidden ? 'bg-green-500' : 'bg-gray-500'}`}
                onClick={() => handleToggleVisibility(review.reviewId, review.isHidden)}
              >
                {review.isHidden ? '공개로 변경' : '숨김으로 변경'}
              </button>
            </div>
            <div className="mb-1">신고수: {review.reportCount || 0}</div>
            <div>
              <button
                className="text-blue-600 underline"
                onClick={() => {
                  router.push(`/admin/review-management/list/${review.reviewId}`);
                }}
              >상세/처리</button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}