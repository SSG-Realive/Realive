'use client';

import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { getReviewDetail, updateReview } from '@/service/customer/reviewService';
import { ReviewResponseDTO } from '@/types/customer/review/review';
import Navbar from '@/components/customer/common/Navbar';

export default function EditReviewPage() {
    const { id } = useParams();
    const router = useRouter();

    const [review, setReview] = useState<ReviewResponseDTO | null>(null);
    const [content, setContent] = useState('');
    const [rating, setRating] = useState(5);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        if (!id) return;
        getReviewDetail(Number(id))
            .then((data) => {
                setReview(data);
                setContent(data.content);
                setRating(data.rating);
            })
            .catch(() => setError('리뷰 정보를 불러올 수 없습니다.'))
            .finally(() => setLoading(false));
    }, [id]);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        try {
            await updateReview(Number(id), { content, rating });
            alert('리뷰가 수정되었습니다.');
            router.push(`/customer/mypage/reviews/${id}`);
        } catch (err) {
            alert('수정에 실패했습니다.');
        }
    };

    if (loading) return <div>로딩 중...</div>;
    if (error) return <div className="text-red-500">{error}</div>;
    if (!review) return <div>리뷰가 존재하지 않습니다.</div>;

    return (
        <div>
            <Navbar />
            <div className="max-w-2xl mx-auto px-6 py-10">
                <h1 className="text-2xl font-bold mb-6">리뷰 수정</h1>
                <form onSubmit={handleSubmit} className="space-y-4">
                    <div>
                        <label className="block font-medium mb-1">별점</label>
                        <select
                            value={rating}
                            onChange={(e) => setRating(Number(e.target.value))}
                            className="border rounded px-3 py-2 w-full"
                        >
                            {[5, 4, 3, 2, 1].map((value) => (
                                <option key={value} value={value}>
                                    {value}점
                                </option>
                            ))}
                        </select>
                    </div>

                    <div>
                        <label className="block font-medium mb-1">리뷰 내용</label>
                        <textarea
                            value={content}
                            onChange={(e) => setContent(e.target.value)}
                            className="border rounded px-3 py-2 w-full min-h-[120px]"
                        />
                    </div>

                    <button
                        type="submit"
                        className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
                    >
                        저장
                    </button>
                </form>
            </div>
        </div>
    );
}
