"use client";
import React, { useState, useEffect } from "react";
import { useRouter, useParams } from "next/navigation";
import { getAdminReview, updateAdminReview } from "@/service/admin/reviewService";
import { AdminReview } from "@/types/admin/review";
import { getTrafficLightEmoji, getTrafficLightText, getTrafficLightBgClass } from "@/types/admin/review";

export default function ReviewDetailPage() {
  const router = useRouter();
  const params = useParams();
  const reviewId = Number(params.id);
  
  console.log('리뷰 상세 페이지 - params:', params);
  console.log('리뷰 상세 페이지 - reviewId:', reviewId);
  
  const [review, setReview] = useState<AdminReview | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [updating, setUpdating] = useState(false);

  // 리뷰 상세 조회
  const fetchReviewDetail = async () => {
    try {
      setLoading(true);
      setError(null);
      
      const data = await getAdminReview(reviewId);
      setReview(data);
    } catch (err: any) {
      console.error('리뷰 상세 조회 실패:', err);
      setError(err.message || '리뷰 상세 정보를 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  // 초기 로드
  useEffect(() => {
    console.log('리뷰 상세 페이지 - useEffect 실행');
    console.log('리뷰 상세 페이지 - reviewId:', reviewId);
    console.log('리뷰 상세 페이지 - reviewId 타입:', typeof reviewId);
    console.log('리뷰 상세 페이지 - reviewId가 유효한지:', !isNaN(reviewId) && reviewId > 0);
    
    if (reviewId && !isNaN(reviewId) && reviewId > 0) {
      console.log('리뷰 상세 페이지 - API 호출 시작');
      fetchReviewDetail();
    } else {
      console.error('리뷰 상세 페이지 - 유효하지 않은 reviewId:', reviewId);
      setError('유효하지 않은 리뷰 ID입니다.');
      setLoading(false);
    }
  }, [reviewId]);

  // 상태 변경
  const handleStatusChange = async (isHidden: boolean) => {
    if (!review) return;

    const statusText = isHidden ? "숨김" : "정상";
    if (!confirm(`리뷰 상태를 "${statusText}"로 변경하시겠습니까?`)) {
      return;
    }

    try {
      setUpdating(true);
      await updateAdminReview(reviewId, isHidden);
      alert('상태가 변경되었습니다.');
      fetchReviewDetail(); // 상세 정보 새로고침
    } catch (err: any) {
      console.error('상태 변경 실패:', err);
      alert(err.message || '상태 변경에 실패했습니다.');
    } finally {
      setUpdating(false);
    }
  };

  // 상태 텍스트 변환
  const getStatusText = (isHidden: boolean) => {
    return isHidden ? '숨김' : '정상';
  };

  // 상태별 스타일
  const getStatusStyle = (isHidden: boolean) => {
    return isHidden ? 'text-gray-600' : 'text-green-600';
  };

  if (typeof window !== 'undefined' && !localStorage.getItem('adminToken')) {
    window.location.replace('/admin/login');
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
        <div className="flex gap-2 justify-center">
          <button 
            onClick={() => fetchReviewDetail()} 
            className="bg-blue-500 text-white px-4 py-2 rounded"
          >
            다시 시도
          </button>
          <button 
            onClick={() => router.push('/admin/review-management/list')} 
            className="bg-gray-500 text-white px-4 py-2 rounded"
          >
            목록으로
          </button>
        </div>
      </div>
    );
  }

  if (!review) {
    return (
      <div className="p-8">
        <div className="text-center text-gray-500">리뷰를 찾을 수 없습니다.</div>
        <div className="text-center mt-4">
          <button 
            onClick={() => router.push('/admin/review-management/list')} 
            className="bg-gray-500 text-white px-4 py-2 rounded"
          >
            목록으로
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="p-8 max-w-4xl mx-auto">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold">리뷰 상세</h1>
        <button 
          onClick={() => router.push('/admin/review-management/list')}
          className="bg-gray-500 text-white px-4 py-2 rounded hover:bg-gray-600"
        >
          목록으로
        </button>
      </div>

      <div className="bg-white rounded-lg shadow-lg p-6">
        {/* 기본 정보 */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
          <div>
            <h2 className="text-lg font-semibold mb-4">리뷰 정보</h2>
            <div className="space-y-3">
              <div>
                <span className="font-medium">리뷰 ID:</span>
                <span className="ml-2">{review.reviewId}</span>
              </div>
              <div>
                <span className="font-medium">상품명:</span>
                <span className="ml-2">{review.productName}</span>
              </div>
              <div>
                <span className="font-medium">작성자:</span>
                <span className="ml-2">{review.customerName}</span>
              </div>
              <div>
                <span className="font-medium">평점:</span>
                <span className="ml-2">
                  <div className={`inline-flex items-center space-x-2 px-3 py-1 rounded-full border ${getTrafficLightBgClass(review.rating)}`}>
                    <span className="text-xl">{getTrafficLightEmoji(review.rating)}</span>
                    <span className="text-sm font-medium">{getTrafficLightText(review.rating)}</span>
                  </div>
                </span>
              </div>
              <div>
                <span className="font-medium">작성일:</span>
                <span className="ml-2">{new Date(review.createdAt).toLocaleString()}</span>
              </div>
              <div>
                <span className="font-medium">수정일:</span>
                <span className="ml-2">{new Date(review.updatedAt).toLocaleString()}</span>
              </div>
              <div>
                <span className="font-medium">상태:</span>
                <span className={`ml-2 ${getStatusStyle(review.isHidden)}`}>
                  {getStatusText(review.isHidden)}
                </span>
              </div>
              {review.reportCount !== undefined && (
                <div>
                  <span className="font-medium">신고 수:</span>
                  <span className="ml-2">{review.reportCount}</span>
                </div>
              )}
            </div>
          </div>

          {/* 상품 이미지 */}
          {review.productImage && (
            <div>
              <h2 className="text-lg font-semibold mb-4">상품 이미지</h2>
              <img 
                src={review.productImage} 
                alt={review.productName}
                className="w-full max-w-xs h-auto rounded border"
              />
            </div>
          )}
        </div>

        {/* 리뷰 내용 */}
        <div className="mb-8">
          <h2 className="text-lg font-semibold mb-4">리뷰 내용</h2>
          <div className="bg-gray-50 p-4 rounded border">
            <p className="whitespace-pre-wrap">{review.content}</p>
          </div>
        </div>

        {/* 상태 변경 */}
        <div className="border-t pt-6">
          <h2 className="text-lg font-semibold mb-4">상태 관리</h2>
          <div className="flex gap-2">
            <button
              onClick={() => handleStatusChange(false)}
              disabled={updating || !review.isHidden}
              className="px-4 py-2 bg-green-500 text-white rounded hover:bg-green-600 disabled:opacity-50"
            >
              정상으로 변경
            </button>
            <button
              onClick={() => handleStatusChange(true)}
              disabled={updating || review.isHidden}
              className="px-4 py-2 bg-yellow-500 text-white rounded hover:bg-yellow-600 disabled:opacity-50"
            >
              숨김으로 변경
            </button>
          </div>
          {updating && (
            <p className="text-blue-600 mt-2">상태 변경 중...</p>
          )}
        </div>
      </div>
    </div>
  );
} 