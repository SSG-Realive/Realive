'use client';

import { useEffect, useState } from 'react';
import { fetchMyReviews } from '@/service/customer/reviewService';
import { ReviewResponseDTO } from '@/types/customer/review/review';
import Navbar from '@/components/customer/common/Navbar';
import Link from 'next/link';

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
        <div>
            <Navbar />
            <div className="max-w-3xl mx-auto px-4 py-10">
                <h1 className="text-2xl font-bold mb-6">내가 작성한 리뷰</h1>

                {loading ? (
                    <p>로딩 중...</p>
                ) : error ? (
                    <p className="text-red-500">{error}</p>
                ) : reviews.length === 0 ? (
                    <p className="text-gray-500">아직 작성한 리뷰가 없습니다.</p>
                ) : (
                    <div className="space-y-4">
                        {reviews.map((review) => (
                            <Link
                                key={review.reviewId}
                                href={`/customer/mypage/reviews/${review.reviewId}`}
                                className="block border rounded p-4 hover:shadow transition"
                            >
                                <p className="font-semibold">{review.productName}</p>
                                <p className="text-yellow-500">⭐ {review.rating}</p>
                                <p className="text-gray-700 text-sm mt-1 line-clamp-2">{review.content}</p>
                                <p className="text-xs text-gray-400 mt-1">{review.createdAt}</p>
                            </Link>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
}
