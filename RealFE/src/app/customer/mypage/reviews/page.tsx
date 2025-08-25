'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';

import { fetchMyReviews } from '@/service/customer/reviewService';
import { ReviewResponseDTO } from '@/types/customer/review/review';
import {
    getTrafficLightEmoji,
    getTrafficLightText,
    getTrafficLightBgClass
} from '@/types/admin/review';
import MyPageLayout from '@/components/layouts/MyPageLayout';

export default function MyReviewPage() {
    const [reviews, setReviews] = useState<ReviewResponseDTO[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        fetchMyReviews()
            .then((data) => setReviews(data))
            .catch((err) => {
                console.error('리뷰 불러오기 실패:', err);
                setError('리뷰를 불러오는 중 문제가 발생했습니다.');
            })
            .finally(() => setLoading(false));
    }, []);

    return (
        <MyPageLayout>
            <div className="max-w-3xl mx-auto px-4 py-10">
                <p className="text-sm text-gray-500 mb-6">
                    총 <span className="text-[#FF6347]">{reviews.length}</span>개
                </p>

                {loading ? (
                    <p>로딩 중...</p>
                ) : error ? (
                    <p className="text-red-500">{error}</p>
                ) : reviews.length === 0 ? (
                    <p className="text-gray-500">아직 작성한 리뷰가 없습니다.</p>
                ) : (
                    <div className="space-y-8">
                        {reviews.map((review, index) => (
                            <Link
                                key={review.reviewId}
                                href={`/customer/mypage/reviews/${review.reviewId}`}
                                className="block border border-gray-200 rounded-md p-4 hover:bg-gray-50 transition"
                            >
                                {/* 상단 선 + 판매자 정보 */}
                                <div className="relative mb-4 sm:mb-2">
                                    <hr className="border-t border-gray-200" />
                                    <div className="absolute -top-3 left-0 flex items-center space-x-1 bg-white px-2 text-sm text-gray-600">
                                        <span>판매자 {review.sellerId}</span>
                                    </div>
                                    <span className="absolute top-0 right-0 text-xs text-gray-400">
                                        No.{reviews.length - index}
                                    </span>
                                </div>

                                <p className="font-normal mb-2 sm:mb-1">{review.productName}</p>

                                <div className="flex justify-end items-center mb-3 sm:mb-1">
                                    <div
                                        className={`flex items-center gap-1 px-3 py-1 rounded-full border ${getTrafficLightBgClass(
                                            review.rating
                                        )} bg-white`}
                                    >
                                        <span className="text-base">
                                            {getTrafficLightEmoji(review.rating)}
                                        </span>
                                        <span className="text-xs text-gray-700">
                                            {getTrafficLightText(review.rating)}
                                        </span>
                                    </div>
                                </div>

                                <p className="text-gray-700 text-sm mt-2 line-clamp-2">
                                    {review.content}
                                </p>

                                <p className="text-xs text-gray-400 mt-1">
                                    {new Date(review.createdAt).toLocaleString('ko-KR', {
                                        year: 'numeric',
                                        month: '2-digit',
                                        day: '2-digit',
                                        hour: '2-digit',
                                        minute: '2-digit',
                                    })}
                                </p>

                                {review.imageUrls && review.imageUrls.length > 0 && (
                                    <div className="mt-3 flex space-x-2 overflow-x-auto">
                                        {review.imageUrls.map((url, idx) => (
                                            <img
                                                key={idx}
                                                src={url}
                                                alt={`리뷰 ${review.reviewId} 썸네일 ${idx + 1}`}
                                                className="w-24 h-24 object-cover rounded flex-shrink-0"
                                                loading="lazy"
                                            />
                                        ))}
                                    </div>
                                )}
                            </Link>
                        ))}
                    </div>
                )}
            </div>
        </MyPageLayout>
    );
}
