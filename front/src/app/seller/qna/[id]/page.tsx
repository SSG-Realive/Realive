'use client';

import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { getQnaDetail } from '@/service/seller/sellerQnaService';
import { SellerQnaDetailResponse } from '@/types/seller/sellerqna/sellerQnaResponse';
import SellerHeader from '@/components/seller/SellerHeader';
import SellerSidebar from '@/components/seller/SellerSidebar';
import SellerLayout from '@/components/layouts/SellerLayout';

export default function SellerQnaDetailPage() {
    const params = useParams();
    const router = useRouter();

    const id = Number(params.id);

    const [qna, setQna] = useState<SellerQnaDetailResponse | null>(null);
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState(true);


    useEffect(() => {
        const fetchData = async () => {
            try {
                setLoading(true);
                const res = await getQnaDetail(id);
                setQna(res);
                setError(null);
            } catch (err) {
                console.error('QnA 상세 조회 실패:', err);
                setError('QnA 데이터를 불러오는 데 실패했습니다.');
            } finally {
                setLoading(false);
            }
        };

        if (!isNaN(id)) {
            fetchData();
        }
    }, [id]);

    return (
        <>
        <SellerHeader/>
        <SellerLayout>
        <div className="max-w-3xl mx-auto p-6">
            <button
                onClick={() => router.back()}
                className="mb-4 text-blue-600 hover:underline"
            >
                ← 목록으로 돌아가기
            </button>

            {loading ? (
                <p className="text-gray-500">로딩 중...</p>
            ) : error ? (
                <p className="text-red-500">{error}</p>
            ) : qna ? (
                <div>
                    <p>
                        <strong>판매자:</strong> {qna.sellerName} ({qna.sellerEmail})
                    </p>
                    <p className="mt-2">
                        <strong>제목:</strong> {qna.title}
                    </p>
                    <p className="mt-2">
                        <strong>내용:</strong> {qna.content}
                    </p>
                    <p className="mt-2">
                        <strong>작성일:</strong> {qna.createdAt}
                    </p>

                    {qna.answer ? (
                        <div className="mt-4 p-3 bg-green-100 rounded">
                            ✅ <strong>답변:</strong> {qna.answer}
                        </div>
                    ) : (
                        <div className="mt-4 text-gray-500 italic">
                            답변 대기 중입니다.
                        </div>
                    )}

                    {/* 수정하기 버튼 추가 */}
                    <button
                        onClick={() => router.push(`/seller/qna/${id}/edit`)}
                        className="mt-4 bg-yellow-500 hover:bg-yellow-600 text-white px-4 py-2 rounded"
                    >
                        수정하기
                    </button>
                </div>
            ) : (
                <p className="text-gray-400">QnA 데이터를 찾을 수 없습니다.</p>
            )}
        </div>
        </SellerLayout>
        </>
    );
}
