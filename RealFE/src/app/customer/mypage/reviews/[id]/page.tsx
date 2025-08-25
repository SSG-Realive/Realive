'use client';

import { useRouter, useParams } from 'next/navigation';
import { useEffect, useState } from 'react';
import { fetchReviewDetail, deleteReview } from '@/service/customer/reviewService';
import { ReviewResponseDTO } from '@/types/customer/review/review';
import Link from 'next/link';
import {
    getTrafficLightEmoji,
    getTrafficLightText,
    getTrafficLightBgClass,
} from '@/types/admin/review';
import { useGlobalDialog } from '@/app/context/dialogContext';

export default function ReviewDetailPage() {
    const { id } = useParams();
    const router = useRouter();
    const [review, setReview] = useState<ReviewResponseDTO | null>(null);
    const [error, setError] = useState<string | null>(null);
    const { show } = useGlobalDialog();

    useEffect(() => {
        if (!id) return;
        fetchReviewDetail(Number(id))
            .then(setReview)
            .catch(() => setError('리뷰 정보를 불러오지 못했습니다.'));
    }, [id]);

    const handleDelete = async () => {
        if (!confirm('정말 삭제하시겠습니까?')) return;
        try {
            await deleteReview(Number(id));
            show('리뷰가 삭제되었습니다.');
            router.push('/customer/mypage/reviews');
        } catch (err) {
            show('삭제에 실패했습니다.');
        }
    };

    if (error) return <div className="text-red-500">{error}</div>;
    if (!review) return <div>로딩 중...</div>;

    return (
        <div className="max-w-6xl mx-auto px-6 py-12">
            <h1 className="text-xl font-light mb-8">{review.productName}</h1>

            {/* 좌우 레이아웃 */}
            <div className="flex flex-col lg:flex-row gap-12 min-h-[300px] items-start">
                {/* 왼쪽: 이미지 */}
                <div className="lg:w-1/2 flex justify-center items-start min-h-[200px]">
                    {review.imageUrls && review.imageUrls.length > 0 ? (
                        <div className="flex flex-wrap gap-4 justify-center">
                            {review.imageUrls.map((url, idx) => (
                                <img
                                    key={idx}
                                    src={url}
                                    alt={`리뷰 이미지 ${idx + 1}`}
                                    className="w-48 h-48 object-cover rounded-md"
                                />
                            ))}
                        </div>
                    ) : (
                        <div className="text-gray-400 text-sm">이미지가 없습니다.</div>
                    )}
                </div>

                {/* 오른쪽: 평가 + 내용 + 버튼 */}
                <div className="lg:w-1/2 flex flex-col justify-between w-full">
                    {/* 평가 + 날짜 (좌우 정렬) */}
                    <div className="flex justify-between items-center mb-4 w-full">
                        <div
                            className={`flex items-center gap-2 px-3 py-1 rounded-full border ${getTrafficLightBgClass(
                                review.rating
                            )} bg-white`}
                        >
                            <span className="text-lg">{getTrafficLightEmoji(review.rating)}</span>
                            <span className="text-sm text-gray-700">{getTrafficLightText(review.rating)}</span>
                        </div>
                        <p className="text-xs text-gray-500 whitespace-nowrap">
                            {new Date(review.createdAt).toLocaleString('ko-KR', {
                                year: 'numeric',
                                month: '2-digit',
                                day: '2-digit',
                                hour: '2-digit',
                                minute: '2-digit',
                            })}
                        </p>
                    </div>

                    {/* 본문 */}
                    <p
                        className="text-gray-800 whitespace-pre-line mb-6 border border-gray-200 rounded-mdpx-6 py-6 leading-relaxed text-base bg-white min-h-[250px]"
                    >
                        {review.content}
                    </p>

                    {/* 버튼 */}
                    <div className="flex gap-2 mt-auto">
                        <button
                            onClick={() => router.push(`/customer/mypage/reviews/${id}/edit`)}
                            className="w-1/2 px-4 py-2 bg-black text-white rounded hover:bg-neutral-800"
                        >
                            수정
                        </button>
                        <button
                            onClick={handleDelete}
                            className="w-1/2 px-4 py-2 bg-black text-white rounded hover:bg-neutral-800"
                        >
                            삭제
                        </button>
                    </div>
                </div>
            </div>

            {/* 관련 상품 */}
            {review.productSummaryList && review.productSummaryList.length > 0 && (
                <div className="mt-12">
                    <h3 className="text-lg font-light text-gray-600 mb-3">관련 상품</h3>

                    {/* 슬라이더 형식으로 */}
                    <div className="flex overflow-x-auto gap-4 no-scrollbar">
                        {review.productSummaryList.map(product => (
                            <Link
                                key={product.id}
                                href={`/main/products/${product.id}`}
                                className="flex-shrink-0 w-40"
                            >
                                <img
                                    src={product.imageThumbnailUrl}
                                    alt={product.name}
                                    className="w-full h-32 object-cover rounded mb-2"
                                />
                                <p className="text-sm text-gray-700 text-center">{product.name}</p>
                            </Link>
                        ))}
                    </div>
                </div>
            )}
        </div>
    );
}
