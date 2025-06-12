'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { getQnaList } from '@/service/sellerQnaService';
import { SellerQnaResponse } from '@/types/sellerQna';
import QnaListItem from '@/components/sellerQna/QnaListItem';
// import useSellerAuthGuard from '@/hooks/useSellerAuthGuard';

import SellerLayout from '@/components/layouts/SellerLayout';
import Header from '@/components/Header';

export default function SellerQnaPage() {
    // useSellerAuthGuard();

    const router = useRouter();
    const [qnaList, setQnaList] = useState<SellerQnaResponse[]>([]);
    const [totalPages, setTotalPages] = useState(1);
    const [page, setPage] = useState(1);
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchData = async () => {
            try {
                setLoading(true);
                const res = await getQnaList({ page, size: 10 });
                setQnaList(res.dtoList || []);
                setTotalPages(Math.ceil((res.total || 1) / (res.size || 10)));
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
        <SellerLayout>
            <Header />
            <div className="max-w-3xl mx-auto p-6">
                <div className="flex justify-between items-center mb-4">
                    <h1 className="text-2xl font-bold">판매자 QnA 목록</h1>
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
                            qnaList.map((qna) => <QnaListItem key={qna.id} qna={qna} />)
                        ) : (
                            <li className="text-gray-400">등록된 질문이 없습니다.</li>
                        )}
                    </ul>
                )}

                <div className="mt-4 flex gap-2 justify-center items-center">
                    <button
                        onClick={() => setPage((p) => Math.max(p - 1, 1))}
                        disabled={page === 1}
                        className="px-3 py-1 border rounded disabled:opacity-50"
                    >
                        이전
                    </button>
                    <span className="text-sm">
                        {page} / {totalPages}
                    </span>
                    <button
                        onClick={() => setPage((p) => Math.min(p + 1, totalPages))}
                        disabled={page === totalPages}
                        className="px-3 py-1 border rounded disabled:opacity-50"
                    >
                        다음
                    </button>
                </div>
            </div>
        </SellerLayout>
    );
}