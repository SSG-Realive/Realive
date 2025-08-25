'use client';

import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { getCustomerQnaDetail, answerCustomerQna } from '@/service/seller/customerQnaService';
import { CustomerQnaDetailResponse } from '@/types/seller/customerqna/customerQnaResponse';
import SellerHeader from '@/components/seller/SellerHeader';
import SellerSidebar from '@/components/seller/SellerSidebar';
import SellerLayout from '@/components/layouts/SellerLayout';
import { User, Package, MessageCircle, CheckCircle, Clock } from 'lucide-react';

export default function SellerQnaDetailPage() {
    const params = useParams();
    const router = useRouter();

    const id = Number(params.id);

    const [qna, setQna] = useState<CustomerQnaDetailResponse | null>(null);
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState(true);
    const [answer, setAnswer] = useState('');
    const [submitting, setSubmitting] = useState(false);

    useEffect(() => {
        const fetchData = async () => {
            try {
                setLoading(true);
                const res = await getCustomerQnaDetail(id);
                setQna(res.qna);
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
            alert('답변 내용을 입력해주세요.');
            return;
        }

        try {
            setSubmitting(true);
            await answerCustomerQna(id, answer);
            // 답변 등록 후 상세 데이터 재조회
            const res = await getCustomerQnaDetail(id);
            setQna(res.qna);
            setAnswer('');
            alert('답변이 등록되었습니다.');
        } catch (err) {
            console.error('답변 등록 실패:', err);
            alert('답변 등록에 실패했습니다.');
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
                className="mb-4 text-[#bfa06a] hover:text-[#5b4636] hover:underline flex items-center gap-2"
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
                    <div className="bg-[#e9dec7] rounded-lg shadow-sm border border-[#bfa06a] p-4 sm:p-6">
                        <div className="flex flex-col sm:flex-row sm:items-center gap-4 mb-4">
                            <div className="flex items-center gap-2">
                                <User className="w-5 h-5 text-[#bfa06a]" />
                                <span className="text-[#5b4636] font-semibold">고객: {qna.customerName}</span>
                            </div>
                            <div className="flex items-center gap-2">
                                <Package className="w-5 h-5 text-[#bfa06a]" />
                                <span className="text-[#5b4636] font-semibold">상품: {qna.productName}</span>
                            </div>
                        </div>
                        <div className="flex items-center gap-2">
                            {qna.answered === true || qna.answered === 'true' ? (
                                <span className="px-2 py-1 rounded text-xs font-bold bg-[#e9dec7] text-[#5b4636] flex items-center gap-1">
                                    <CheckCircle className="w-3 h-3" />
                                    답변 완료
                                </span>
                            ) : (
                                <span className="px-2 py-1 rounded text-xs font-bold bg-[#fbeee0] text-[#b94a48] flex items-center gap-1">
                                    <Clock className="w-3 h-3" />
                                    미답변
                                </span>
                            )}
                        </div>
                    </div>

                    {/* 문의 내용 */}
                    <div className="bg-[#e9dec7] rounded-lg shadow-sm border border-[#bfa06a] p-4 sm:p-6">
                        <h1 className="text-xl sm:text-2xl font-bold mb-4 text-[#5b4636] break-words">{qna.title}</h1>
                        
                        <div className="space-y-3 mb-4">
                            <div className="flex flex-col sm:flex-row sm:items-center gap-2">
                                <span className="text-sm font-medium text-[#bfa06a] min-w-[80px]">작성일:</span>
                                <span className="text-[#5b4636]">{qna.createdAt}</span>
                            </div>
                        </div>

                        <div className="bg-[#f5f1eb] rounded-lg p-4 border border-[#bfa06a]">
                            <h3 className="text-sm font-semibold text-[#bfa06a] mb-2">문의 내용</h3>
                            <p className="text-[#5b4636] whitespace-pre-wrap">{qna.content}</p>
                        </div>
                    </div>

                    {/* 답변 영역 */}
                    {qna && (qna.answered === true || qna.answered === 'true') ? (
                        <div className="bg-[#e9dec7] rounded-lg shadow-sm border border-[#bfa06a] p-4 sm:p-6">
                            <div className="bg-[#f5f1eb] rounded-lg p-4 border border-[#bfa06a]">
                                <h3 className="text-sm font-semibold text-[#bfa06a] mb-2">답변 내용</h3>
                                <p className="text-[#5b4636] whitespace-pre-wrap">{qna.answer ? qna.answer : '답변 내용이 없습니다.'}</p>
                                <div className="mt-3 text-xs text-[#bfa06a]">
                                    답변일: {qna.answeredAt || '-' }</div>
                            </div>
                        </div>
                    ) : (
                        <div className="bg-[#e9dec7] rounded-lg shadow-sm border border-[#bfa06a] p-4 sm:p-6">
                            <h3 className="text-lg font-semibold text-[#5b4636] mb-4">답변 작성</h3>
                            <textarea
                                value={answer}
                                onChange={(e) => setAnswer(e.target.value)}
                                placeholder="고객 문의에 대한 답변을 입력해주세요..."
                                className="w-full h-32 p-3 border border-[#bfa06a] rounded-lg bg-[#f5f1eb] text-[#5b4636] placeholder-[#bfa06a] focus:outline-none focus:ring-2 focus:ring-[#bfa06a] resize-none"
                            />
                            <div className="mt-4 flex gap-2">
                                <button
                                    onClick={handleSubmitAnswer}
                                    disabled={submitting || !answer.trim()}
                                    className="bg-[#bfa06a] text-[#4b3a2f] px-4 py-2 rounded hover:bg-[#5b4636] hover:text-[#e9dec7] disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
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
