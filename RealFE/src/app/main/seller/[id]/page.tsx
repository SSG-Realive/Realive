// src/app/main/seller/[id]/page.tsx

import React from 'react';

import {
  getSellerPublicInfoBySellerId,
  getSellerReviews,
  getSellerProducts
} from '@/service/seller/sellerService';

import ClientSellerDetails from './ClientSellerDetails';
import ImageWithFallback from '@/components/common/imageWithFallback';
import { getTrafficLightText } from '@/types/admin/review';


export const dynamic = "force-dynamic";


const getRatingColor = (rating: number): string => {
  if (rating >= 66.1) return 'bg-green-500';
  if (rating >= 33.1) return 'bg-yellow-400';
  return 'bg-red-500';
};

type ParamsType = { id: string };

export default async function SellerDetailPage(props: any) {
  const sellerId = parseInt(props.params?.id, 10);

  if (isNaN(sellerId)) {
    return (
      <div className="flex justify-center items-center h-screen text-red-500 text-xl font-light">
        잘못된 판매자 ID입니다.
      </div>
    );
  }

  const seller = await getSellerPublicInfoBySellerId(sellerId);

  if (!seller) {
    return (
      <div className="flex justify-center items-center h-screen text-red-500 text-xl font-light">
        판매자 정보를 찾을 수 없습니다.
      </div>
    );
  }


  const [initialReviewsResponse, initialProductsResponse] = await Promise.all([
    getSellerReviews(sellerId, 0),
    getSellerProducts(sellerId, 0)
  ]);

  const initialReviews = initialReviewsResponse.reviews;
  const initialHasMoreReviews = initialReviewsResponse.hasMore;
  const initialProducts = initialProductsResponse.products;
  const initialHasMoreProducts = initialProductsResponse.hasMore;

  const displayAverageRatingPercentile = (seller.averageRating / 5) * 100;
  const ratingColorClass = getRatingColor(displayAverageRatingPercentile);

  function SmallTrafficLightCard({ rating, title, count }: { rating: number; title: string; count?: number }) {
    const getCircleColor = (rating: number) => {
      if (rating <= 2 && rating > 0) return '#ef4444';
      if (rating === 3) return '#facc15';
      if (rating >= 4) return '#22c55e';
      return '#d1d5db';
    };

    const isNoReview = !count || rating === 0;

    return (
      <div
        className="
          rounded-lg border border-blue-100 px-3 py-2 sm:px-4 sm:py-3 w-full sm:w-44 shadow flex flex-row sm:flex-col items-center sm:items-center gap-3
        "
      >
        <svg width="32" height="32" viewBox="0 0 56 56" className="flex-shrink-0">
          <circle
            cx="28"
            cy="28"
            r="24"
            fill={getCircleColor(rating)}
            stroke="#d6ccc2"
            strokeWidth="4"
            style={{ filter: isNoReview ? 'grayscale(1)' : 'drop-shadow(0 0 4px #fff)' }}
          />
        </svg>
        <div className="flex flex-col text-center sm:text-center">
          <div className="text-xs sm:text-sm font-light text-gray-700">{title}</div>
          <div className="text-sm sm:text-base font-light text-gray-900">
            {isNoReview ? '평가 없음' : getTrafficLightText(rating)}
          </div>
          <div className="text-[9px] sm:text-xs text-gray-500 mt-1">리뷰 {count ?? 0}건</div>
        </div>
      </div>
    );
  }

  return (
    <div className="container mx-auto p-4 max-w-4xl font-inter antialiased">
      {/* 판매자 헤더 섹션 */}
      <div className="flex flex-col md:flex-row justify-between items-start gap-6 mb-10 p-6 rounded-xl shadow-md border border-blue-100">
        {/* 왼쪽: 프로필 + 정보 */}
        <div className="flex items-start gap-6">
          <div className="relative w-36 h-36 flex-shrink-0 rounded-full overflow-hidden bg-gray-200 border-2 border-gray-300 shadow-sm">
            <ImageWithFallback
              src={seller.profileImageUrl}
              alt="판매자 프로필 이미지"
              className="w-full h-full object-cover"
              fallbackSrc="https://placehold.co/128x128/e0e0e0/555555?text=Profile"
            />
            <div
              className={`absolute -top-1 -left-1 w-6 h-6 rounded-full flex items-center justify-center text-white text-xs font-light ${ratingColorClass} border-2 border-white shadow-md`}
            >
              {displayAverageRatingPercentile.toFixed(0)}%
            </div>
          </div>

          <div>
            <h1 className="text-xl font-light text-gray-800 mb-2">{seller.name}</h1>
            <div className="text-sm text-gray-600 mb-1">
              <span className="font-light text-gray-700">연락처:</span> {seller.contactNumber || '정보 없음'}
            </div>
            <div className="text-sm text-gray-600 mb-1">
              <span className="font-light text-gray-700">사업자 번호:</span> {seller.businessNumber || '정보 없음'}
            </div>
            <div className="text-sm text-gray-600 mb-1">
              <span className="font-light text-gray-700">가입일:</span>{' '}
              {seller.createdAt ? new Date(seller.createdAt).toLocaleDateString('ko-KR') : '정보 없음'}
            </div>
            <div className="text-sm text-gray-600 mt-1">
              <span className="font-light text-gray-700">총 리뷰 수:</span> {seller.totalReviews}
            </div>
          </div>
        </div>

        {/* 오른쪽 하단: 신호등 카드 */}
        <SmallTrafficLightCard
          title="평가 등급"
          rating={seller.averageRating}
          count={seller.totalReviews}
        />
      </div>

      {/* 판매자 상세 정보 (리뷰/상품) */}
      <ClientSellerDetails
        sellerId={sellerId}
        initialReviews={initialReviews}
        initialHasMoreReviews={initialHasMoreReviews}
        initialProducts={initialProducts}
        initialHasMoreProducts={initialHasMoreProducts}
      />
    </div>
  );
}
