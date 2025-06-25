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
    const [sidebarOpen, setSidebarOpen] = useState(false);

    const toggleSidebar = () => {
        setSidebarOpen(!sidebarOpen);
    };

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
            <div className="hidden">
            <SellerHeader toggleSidebar={toggleSidebar} />
            </div>
            <SellerLayout> {/* ✅ 명시적 적용 */}
                <div className="flex-1 w-full h-full px-4 py-8 bg-gray-100">
                    <h1 className="text-xl md:text-2xl font-bold mb-4 md:mb-6">판매자 QnA 목록</h1>

                    <div className="flex justify-end mb-4 md:mb-6">
                        <button
                            onClick={() => router.push('/seller/qna/new')}
                            className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 transition-colors"
                        >
                            QnA 등록
                        </button>
                    </div>

                    {loading ? (
                        <div className="flex items-center justify-center py-8">
                            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
                            <span className="ml-3 text-gray-600">로딩 중...</span>
                        </div>
                    ) : error ? (
                        <div className="bg-red-50 border border-red-200 rounded-lg p-4">
                            <p className="text-red-600">{error}</p>
                        </div>
                    ) : (
                        <div className="grid gap-4">
                            {Array.isArray(qnaList) && qnaList.length > 0 ? (
                                qnaList.map((qna) => (
                                    <div
                                        key={qna.id}
                                        onClick={() => router.push(`/seller/qna/${qna.id}`)}
                                        className="bg-white border border-gray-200 rounded-lg p-4 shadow-sm hover:shadow-md transition-all cursor-pointer"
                                    >
                                        <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-3">
                                            <div className="flex-1">
                                                <h3 className="font-semibold text-lg text-gray-800 mb-2">{qna.title}</h3>
                                                <div className="flex items-center gap-2">
                                                    <span className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${
                                                        qna.isAnswered 
                                                            ? 'bg-green-100 text-green-800' 
                                                            : 'bg-red-100 text-red-800'
                                                    }`}>
                                            {qna.isAnswered ? '✅ 답변 완료' : '❌ 미답변'}
                                                    </span>
                                                </div>
                                            </div>
                                            <div className="text-sm text-gray-500">
                                                클릭하여 상세보기
                                            </div>
                                        </div>
                                    </div>
                                ))
                            ) : (
                                <div className="bg-white border border-gray-200 rounded-lg p-8 text-center">
                                    <p className="text-gray-500 text-lg">등록된 질문이 없습니다.</p>
                                </div>
                            )}
                        </div>
                    )}

                    {totalPages > 1 && (
                        <div className="mt-6 flex gap-2 justify-center items-center">
                        <button
                            onClick={() => setPage((p) => Math.max(p - 1, 0))}
                            disabled={page === 0}
                                className="px-3 py-2 border border-gray-300 rounded-md disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500 transition-colors"
                        >
                            이전
                        </button>
                            <span className="text-sm px-4 py-2 bg-white border border-gray-300 rounded-md">
                        {page + 1} / {totalPages}
                    </span>
                        <button
                            onClick={() => setPage((p) => Math.min(p + 1, totalPages - 1))}
                            disabled={page >= totalPages - 1}
                                className="px-3 py-2 border border-gray-300 rounded-md disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500 transition-colors"
                        >
                            다음
                        </button>
                    </div>
                    )}
                </div>
            </SellerLayout>
        </>
    );
}