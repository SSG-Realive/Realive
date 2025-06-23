'use client';

import { useRouter, useParams } from 'next/navigation';
import { useEffect, useState } from 'react';
import { getReviewDetail, deleteReview } from '@/service/customer/reviewService';
import { ReviewResponseDTO } from '@/types/customer/review/review';
import Navbar from '@/components/customer/common/Navbar';

export default function ReviewDetailPage() {
    const { id } = useParams();
    const router = useRouter();
    const [review, setReview] = useState<ReviewResponseDTO | null>(null);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        if (!id) return;
        getReviewDetail(Number(id))
            .then(setReview)
            .catch(() => setError('리뷰 정보를 불러오지 못했습니다.'));
    }, [id]);

    const handleDelete = async () => {
        if (!confirm('정말 삭제하시겠습니까?')) return;
        try {
            await deleteReview(Number(id));
            alert('리뷰가 삭제되었습니다.');
            router.push('/customer/mypage/reviews'); // ✅ 목록 페이지로 이동
        } catch (err) {
            alert('삭제에 실패했습니다.');
        }
    };

    if (error) return <div className="text-red-500">{error}</div>;
    if (!review) return <div>로딩 중...</div>;

    return (
        <div>
            <Navbar />
            <div className="max-w-2xl mx-auto px-6 py-10">
                <h1 className="text-2xl font-bold mb-2">{review.productName}</h1>
                <p className="text-yellow-500">⭐ {review.rating}</p>
                <p className="text-gray-700 whitespace-pre-line mt-4">{review.content}</p>
                <p className="text-xs text-gray-400 mt-2">{review.createdAt}</p>

                {/* 버튼 영역 */}
                <div className="mt-6 flex gap-4">
                    <button
                        onClick={() => router.push(`/customer/mypage/reviews/${id}/edit`)} // ✅ 수정 링크
                        className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
                    >
                        수정
                    </button>
                    <button
                        onClick={handleDelete}
                        className="px-4 py-2 bg-red-500 text-white rounded hover:bg-red-600"
                    >
                        삭제
                    </button>
                </div>
            </div>
        </div>
    );
}
