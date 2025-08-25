"use client";
import React, { useState, useEffect } from "react";
import { useRouter, useParams } from "next/navigation";
import { getAdminReview } from "@/service/admin/reviewService";
import { AdminReview } from "@/types/admin/review";
import { useAdminAuthStore } from "@/store/admin/useAdminAuthStore";
import { getTrafficLightEmoji, getTrafficLightText, getTrafficLightBgClass } from "@/types/admin/review";

export default function ReviewDetailPage() {
  const router = useRouter();
  const params = useParams();
  const { accessToken } = useAdminAuthStore();
  
  // params.id를 안전하게 파싱
  const reviewId = params.id ? Number(params.id) : null;
  
  const [review, setReview] = useState<AdminReview | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchReviewDetail = async () => {
    if (!accessToken || !reviewId || isNaN(reviewId)) {
      setError("유효하지 않은 리뷰 ID입니다.");
      setLoading(false);
      return;
    }

    try {
      setLoading(true);
      setError(null);
      const data = await getAdminReview(reviewId);
      setReview(data);
    } catch (err: any) {
      console.error("리뷰 상세 조회 실패:", err);
      setError(err.message || "리뷰 정보를 불러오는데 실패했습니다.");
      if (err.response?.status === 403) {
        router.replace('/admin/login');
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (accessToken) {
      fetchReviewDetail();
    } else {
      router.replace('/admin/login');
    }
  }, [accessToken, reviewId]);

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
          onClick={() => router.back()}
          className="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600"
        >
          목록으로 돌아가기
        </button>
      </div>
    );
  }

  if (!review) {
    return (
      <div className="p-8">
        <div className="text-center">리뷰 정보를 찾을 수 없습니다.</div>
        <button 
          onClick={() => router.back()}
          className="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600 mt-4"
        >
          목록으로 돌아가기
        </button>
      </div>
    );
  }

  return (
    <div className="p-8 max-w-4xl mx-auto">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold">리뷰 상세</h1>
        <button 
          onClick={() => router.back()}
          className="bg-gray-500 text-white px-4 py-2 rounded hover:bg-gray-600"
        >
          목록으로
        </button>
      </div>

      <div className="bg-white rounded-lg shadow-lg p-6 space-y-6">
        {/* 리뷰 기본 정보 */}
        <div>
          <h2 className="text-lg font-semibold border-b pb-2 mb-4">리뷰 정보</h2>
          <div className="grid grid-cols-2 gap-4">
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
              <span className="font-medium">판매자:</span>
              <span className="ml-2">{review.sellerName}</span>
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
              <span className="font-medium">상태:</span>
              <span className={`ml-2 font-semibold ${review.isHidden ? 'text-red-600' : 'text-green-600'}`}>
                {review.isHidden ? '숨김' : '공개'}
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
          </div>
        </div>

        {/* 리뷰 내용 */}
        <div>
          <h2 className="text-lg font-semibold border-b pb-2 mb-4">리뷰 내용</h2>
          <div className="p-4 bg-gray-50 rounded min-h-[100px]">
            <p className="whitespace-pre-wrap">{review.content}</p>
          </div>
        </div>

        {/* 상품 이미지 (있는 경우) */}
        {review.productImage && (
          <div>
            <h2 className="text-lg font-semibold border-b pb-2 mb-4">상품 이미지</h2>
            <img 
              src={review.productImage} 
              alt="상품 이미지" 
              className="max-w-xs rounded"
            />
          </div>
        )}
      </div>
    </div>
  );
} 