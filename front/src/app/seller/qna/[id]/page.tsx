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
        <div className="max-w-3xl mx-auto p-4 sm:p-6">
            <button
                onClick={() => router.back()}
                className="mb-4 text-blue-600 hover:text-blue-800 hover:underline flex items-center gap-2"
            >
                <span>←</span>
                <span>목록으로 돌아가기</span>
            </button>

            {loading ? (
                <div className="text-center py-8">
                    <p className="text-gray-500">로딩 중...</p>
                </div>
            ) : error ? (
                <div className="text-center py-8">
                    <p className="text-red-500">{error}</p>
                </div>
            ) : qna ? (
                <div className="space-y-6">
                    <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-4 sm:p-6">
                        <h1 className="text-xl sm:text-2xl font-bold mb-4 text-gray-900 break-words">{qna.title}</h1>
                        
                        <div className="space-y-3">
                            <div className="flex flex-col sm:flex-row sm:items-center gap-2">
                                <span className="text-sm font-medium text-gray-600 min-w-[80px]">판매자:</span>
                                <span className="text-gray-900">{qna.sellerName} ({qna.sellerEmail})</span>
                            </div>
                            <div className="flex flex-col sm:flex-row sm:items-center gap-2">
                                <span className="text-sm font-medium text-gray-600 min-w-[80px]">작성일:</span>
                                <span className="text-gray-900">{qna.createdAt}</span>
                            </div>
                        </div>
                    </div>

                    <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-4 sm:p-6">
                        <h2 className="text-lg font-semibold mb-3 text-gray-900">질문 내용</h2>
                        <div className="bg-gray-50 rounded-lg p-4">
                            <p className="text-gray-900 whitespace-pre-wrap break-words">{qna.content}</p>
                        </div>
                    </div>

                    {qna.answer ? (
                        <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-4 sm:p-6">
                            <h2 className="text-lg font-semibold mb-3 text-gray-900 flex items-center gap-2">
                                <span className="text-green-600">✅</span>
                                답변
                            </h2>
                            <div className="bg-green-50 rounded-lg p-4">
                                <p className="text-gray-900 whitespace-pre-wrap break-words">{qna.answer}</p>
                            </div>
                        </div>
                    ) : (
                        <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-4 sm:p-6">
                            <h2 className="text-lg font-semibold mb-3 text-gray-900">답변</h2>
                            <div className="bg-yellow-50 rounded-lg p-4 text-center">
                                <p className="text-gray-600 italic">답변 대기 중입니다.</p>
                            </div>
                        </div>
                    )}

                    <div className="flex justify-center sm:justify-start">
                        <button
                            onClick={() => router.push(`/seller/qna/${id}/edit`)}
                            className="bg-yellow-500 hover:bg-yellow-600 text-white px-6 py-3 rounded-lg font-medium transition-colors"
                        >
                            수정하기
                        </button>
                    </div>
                </div>
            ) : (
                <div className="text-center py-8">
                    <p className="text-gray-400">QnA 데이터를 찾을 수 없습니다.</p>
                </div>
            )}
        </div>
        </SellerLayout>
        </>
    );
}
