'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { getQnaList } from '@/service/seller/sellerQnaService';
import { SellerQnaResponse } from '@/types/seller/sellerqna/sellerQnaResponse';
import SellerLayout from '@/components/layouts/SellerLayout';
import useSellerAuthGuard from '@/hooks/useSellerAuthGuard';
import SellerHeader from '@/components/seller/SellerHeader';

export default function SellerQnaPage() {
    useSellerAuthGuard(); // ✅ 가드 적용 (일관성 유지)

    const router = useRouter();

    const [qnaList, setQnaList] = useState<SellerQnaResponse[]>([]);
    const [totalPages, setTotalPages] = useState(1);
    const [page, setPage] = useState(0);
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchData = async () => {
            try {
                setLoading(true);
                const res = await getQnaList({ page, size: 10 });
                setQnaList(res.content || []);
                setTotalPages(res.totalPages || 1);
                setError(null);
            } catch (err) {
                console.error('QnA 목록 조회 실패:', err);
                setError('QnA 데이터를 불러오는 데 실패했습니다.');
            } finally {
                setLoading(false);
            }
        };
        fetchData();
    }, [page]);

    return (
        <>
            <SellerHeader/>
            <SellerLayout> {/* ✅ 명시적 적용 */}
                <div className="max-w-xl mx-auto py-10">
                    <h1 className="text-2xl font-semibold mb-6">판매자 QnA 목록</h1>

                    <div className="flex justify-end mb-4">
                        <button
                            onClick={() => router.push('/seller/qna/new')}
                            className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded"
                        >
                            QnA 등록
                        </button>
                    </div>

                    {loading ? (
                        <p className="text-gray-500">로딩 중...</p>
                    ) : error ? (
                        <p className="text-red-500">{error}</p>
                    ) : (
                        <ul>
                            {Array.isArray(qnaList) && qnaList.length > 0 ? (
                                qnaList.map((qna) => (
                                    <li
                                        key={qna.id}
                                        onClick={() => router.push(`/seller/qna/${qna.id}`)}
                                        className="mb-2 border p-3 rounded hover:bg-gray-50 cursor-pointer"
                                    >
                                        <div className="font-medium">{qna.title}</div>
                                        <div className="text-sm text-gray-600">
                                            {qna.isAnswered ? '✅ 답변 완료' : '❌ 미답변'}
                                        </div>
                                    </li>
                                ))
                            ) : (
                                <li className="text-gray-400">등록된 질문이 없습니다.</li>
                            )}
                        </ul>
                    )}

                    <div className="mt-4 flex gap-2 justify-center items-center">
                        <button
                            onClick={() => setPage((p) => Math.max(p - 1, 0))}
                            disabled={page === 0}
                            className="px-3 py-1 border rounded disabled:opacity-50"
                        >
                            이전
                        </button>
                        <span className="text-sm">
                        {page + 1} / {totalPages}
                    </span>
                        <button
                            onClick={() => setPage((p) => Math.min(p + 1, totalPages - 1))}
                            disabled={page >= totalPages - 1}
                            className="px-3 py-1 border rounded disabled:opacity-50"
                        >
                            다음
                        </button>
                    </div>
                </div>
            </SellerLayout>
        </>
    );
}