'use client';

import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { getCustomerQnaDetail, answerCustomerQna } from '@/service/seller/customerQnaService';
import { CustomerQnaDetailResponse } from '@/types/seller/customerqna/customerQnaResponse';
import SellerHeader from '@/components/seller/SellerHeader';
import SellerSidebar from '@/components/seller/SellerSidebar';
import SellerLayout from '@/components/layouts/SellerLayout';
import { User, Package, MessageCircle, CheckCircle, Clock } from 'lucide-react';
import { useGlobalDialog } from '@/app/context/dialogContext';

export default function SellerQnaDetailPage() {
    const params = useParams();
    const router = useRouter();

    const id = Number(params.id);

    const [qna, setQna] = useState<CustomerQnaDetailResponse | null>(null);
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState(true);
    const [answer, setAnswer] = useState('');
    const [submitting, setSubmitting] = useState(false);
    const {show} = useGlobalDialog();
    useEffect(() => {
        const fetchData = async () => {
            try {
                setLoading(true);
                const res = await getCustomerQnaDetail(id);
                setQna((res as any).qna);
                setError(null);
            } catch (err) {
                console.error('고객 QnA 상세 조회 실패:', err);
                setError('고객 QnA 데이터를 불러오는 데 실패했습니다.');
            } finally {
                setLoading(false);
            }
        };

        if (!isNaN(id)) {
            fetchData();
        }
    }, [id]);

    // qna 값 콘솔 출력
    useEffect(() => {
        if (qna) {
            console.log('상세 QnA:', qna);
        }
    }, [qna]);

    const handleSubmitAnswer = async () => {
        if (!answer.trim()) {
            show('답변 내용을 입력해주세요.');
            return;
        }

        try {
            setSubmitting(true);
            await answerCustomerQna(id, answer);
            // 답변 등록 후 상세 데이터 재조회
            const res = await getCustomerQnaDetail(id);
            setQna((res as any).qna);
            setAnswer('');
            show('답변이 등록되었습니다.');
            // router.replace('/seller/qna'); // 이 줄을 제거하여 상세 페이지에 머물도록 함
        } catch (err) {
            console.error('답변 등록 실패:', err);
            show('답변 등록에 실패했습니다.');
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <>
        <SellerLayout>
        <div className="max-w-4xl mx-auto p-4 sm:p-6">
            <button
                onClick={() => router.back()}
                className="mb-4 text-[#6b7280] hover:text-[#374151] hover:underline flex items-center gap-2"
            >
                <span>←</span>
                <span>목록으로 돌아가기</span>
            </button>

            {loading ? (
                <div className="text-center py-8">
                    <p className="text-[#5b4636]">로딩 중...</p>
                </div>
            ) : error ? (
                <div className="text-center py-8">
                    <p className="text-[#b94a48]">{error}</p>
                </div>
            ) : qna ? (
                <div className="space-y-6">
                    {/* 고객 및 상품 정보 */}
                    <div className="bg-[#f3f4f6] rounded-lg shadow-sm border border-[#d1d5db] p-4 sm:p-6">
                        <div className="flex flex-col sm:flex-row sm:items-center gap-4 mb-4">
                            <div className="flex items-center gap-2">
                                <User className="w-5 h-5 text-[#6b7280]" />
                                <span className="text-[#374151] font-semibold">고객: {qna.customerName}</span>
                            </div>
                            <div className="flex items-center gap-2">
                                <Package className="w-5 h-5 text-[#6b7280]" />
                                <span className="text-[#374151] font-semibold">상품: {qna.productName}</span>
                            </div>
                        </div>
                        <div className="flex items-center gap-2">
                            {qna.isAnswered || qna.answer ? (
                                <span className="px-2 py-1 rounded text-xs font-bold bg-[#f3f4f6] text-[#374151] flex items-center gap-1">
                                    <CheckCircle className="w-3 h-3" />
                                    답변 완료
                                </span>
                            ) : (
                                <span className="px-2 py-1 rounded text-xs font-bold bg-[#f3f4f6] text-[#374151] flex items-center gap-1">
                                    <Clock className="w-3 h-3" />
                                    미답변
                                </span>
                            )}
                        </div>
                    </div>

                    {/* 문의 내용 */}
                    <div className="bg-[#f3f4f6] rounded-lg shadow-sm border border-[#d1d5db] p-4 sm:p-6">
                        <h1 className="text-xl sm:text-2xl font-bold mb-4 text-[#374151] break-words">{qna.title}</h1>
                        
                        <div className="space-y-3 mb-4">
                            <div className="flex flex-col sm:flex-row sm:items-center gap-2">
                                <span className="text-sm font-medium text-[#6b7280] min-w-[80px]">작성일:</span>
                                <span className="text-[#374151]">{qna.createdAt}</span>
                        </div>
                    </div>

                        <div className="bg-[#f3f4f6] rounded-lg p-4 border border-[#d1d5db]">
                            <h3 className="text-sm font-semibold text-[#6b7280] mb-2">문의 내용</h3>
                            <p className="text-[#374151] whitespace-pre-wrap">{qna.content || '문의 내용이 없습니다.'}</p>
                        </div>
                    </div>

                    {/* 답변 영역 */}
                    {qna && (qna.isAnswered || qna.answer) ? (
                        <div className="bg-[#f3f4f6] rounded-lg shadow-sm border border-[#d1d5db] p-4 sm:p-6">
                            <div className="bg-[#f3f4f6] rounded-lg p-4 border border-[#d1d5db]">
                                <h3 className="text-sm font-semibold text-[#6b7280] mb-2">답변 내용</h3>
                                <p className="text-[#374151] whitespace-pre-wrap">{qna.answer ? qna.answer : '답변 내용이 없습니다.'}</p>
                                <div className="mt-3 text-xs text-[#6b7280]">
                                    답변일: {qna.answeredAt || '-' }</div>
                            </div>
                        </div>
                    ) : (
                        <div className="bg-[#f3f4f6] rounded-lg shadow-sm border border-[#d1d5db] p-4 sm:p-6">
                            <h3 className="text-lg font-semibold text-[#374151] mb-4">답변 작성</h3>
                            <textarea
                                value={answer}
                                onChange={(e) => setAnswer(e.target.value)}
                                placeholder="고객 문의에 대한 답변을 입력해주세요..."
                                className="w-full h-32 p-3 border border-[#d1d5db] rounded-lg bg-[#f3f4f6] text-[#374151] placeholder-[#6b7280] focus:outline-none focus:ring-2 focus:ring-[#d1d5db] resize-none"
                            />
                            <div className="mt-4 flex gap-2">
                                <button
                                    onClick={handleSubmitAnswer}
                                    disabled={submitting || !answer.trim()}
                                    className="bg-[#d1d5db] text-[#374151] px-4 py-2 rounded hover:bg-[#e5e7eb] hover:text-[#374151] disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
                                >
                                    <MessageCircle className="w-4 h-4" />
                                    {submitting ? '답변 등록 중...' : '답변 등록'}
                                </button>
                            </div>
                        </div>
                    )}
                </div>
            ) : (
                <div className="text-center py-8">
                    <p className="text-[#5b4636]">문의 정보를 찾을 수 없습니다.</p>
                </div>
            )}
        </div>
        </SellerLayout>
        </>
    );
}
