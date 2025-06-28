'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { getCustomerQnaList } from '@/service/seller/customerQnaService';
import { CustomerQnaResponse, CustomerQnaListResponse } from '@/types/seller/customerqna/customerQnaResponse';
import SellerLayout from '@/components/layouts/SellerLayout';
import useSellerAuthGuard from '@/hooks/useSellerAuthGuard';
import SellerHeader from '@/components/seller/SellerHeader';
import { MessageCircle, CheckCircle, Clock, Plus, Eye, Search, Filter, User, Package } from 'lucide-react';

export default function SellerQnaPage() {
    const checking = useSellerAuthGuard();

    const router = useRouter();

    const [qnaList, setQnaList] = useState<CustomerQnaResponse[]>([]);
    const [totalPages, setTotalPages] = useState(1);
    const [page, setPage] = useState(0);
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState(true);
    const [sidebarOpen, setSidebarOpen] = useState(false);
    const [searchKeyword, setSearchKeyword] = useState('');
    const [statusFilter, setStatusFilter] = useState('');

    const toggleSidebar = () => {
        setSidebarOpen(!sidebarOpen);
    };

    useEffect(() => {
        if (checking) return;
        
        const fetchData = async () => {
            try {
                setLoading(true);
                const res = await getCustomerQnaList({ page, size: 10 });
                setQnaList(res.content || []);
                setTotalPages(res.totalPages || 1);
                setError(null);
            } catch (err) {
                console.error('고객 QnA 목록 조회 실패:', err);
                setError('고객 QnA 데이터를 불러오는 데 실패했습니다.');
            } finally {
                setLoading(false);
            }
        };
        fetchData();
    }, [page, checking]);

    // 통계 계산
    const totalQna = qnaList.length;
    const answeredQna = qnaList.filter(qna => qna.answered === true || qna.answered === 'true').length;
    const unansweredQna = qnaList.filter(qna => !(qna.answered === true || qna.answered === 'true')).length;

    // 필터링된 QnA 목록
    const filteredQnaList = qnaList.filter(qna => {
        const matchesKeyword = searchKeyword === '' || 
            qna.title.toLowerCase().includes(searchKeyword.toLowerCase()) ||
            qna.content.toLowerCase().includes(searchKeyword.toLowerCase()) ||
            qna.customerName.toLowerCase().includes(searchKeyword.toLowerCase()) ||
            qna.productName.toLowerCase().includes(searchKeyword.toLowerCase());
        
        const matchesStatus = statusFilter === '' || 
            (statusFilter === 'answered' && qna.isAnswered) ||
            (statusFilter === 'unanswered' && !qna.isAnswered);
        
        return matchesKeyword && matchesStatus;
    });

    const getStatusBadge = (isAnswered: boolean) => {
        return isAnswered ? (
            <span className="px-2 py-1 rounded text-xs font-bold bg-[#e9dec7] text-[#5b4636] flex items-center gap-1">
                <CheckCircle className="w-3 h-3" />
                답변 완료
            </span>
        ) : (
            <span className="px-2 py-1 rounded text-xs font-bold bg-[#fbeee0] text-[#b94a48] flex items-center gap-1">
                <Clock className="w-3 h-3" />
                미답변
            </span>
        );
    };

    if (checking || loading) {
        return (
            <div className="w-full max-w-full min-h-screen overflow-x-hidden bg-[#a89f91] flex items-center justify-center">
                <div className="text-center">
                    <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-[#bfa06a] mx-auto mb-4"></div>
                    <p className="text-[#5b4636]">고객 문의 정보를 불러오는 중...</p>
                </div>
            </div>
        );
    }

    return (
        <>
            <div className="hidden">
                <SellerHeader toggleSidebar={toggleSidebar} />
            </div>
            <SellerLayout>
                <div className="flex-1 w-full h-full px-4 py-8 bg-[#a89f91]">
                    <h1 className="text-xl md:text-2xl font-bold mb-6 text-[#5b4636]">고객 문의 관리</h1>

                    {/* 상단 통계 카드 */}
                    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 md:gap-6 mb-6">
                        <section className="bg-[#e9dec7] p-4 md:p-6 rounded-lg shadow-sm border border-[#bfa06a] flex items-center justify-between">
                            <div>
                                <h2 className="text-[#5b4636] text-sm font-semibold mb-2">총 문의 수</h2>
                                <p className="text-xl md:text-2xl font-bold text-[#5b4636]">{totalQna}건</p>
                            </div>
                            <MessageCircle className="w-8 h-8 text-[#bfa06a]" />
                        </section>
                        <section className="bg-[#e9dec7] p-4 md:p-6 rounded-lg shadow-sm border border-[#bfa06a] flex items-center justify-between">
                            <div>
                                <h2 className="text-[#5b4636] text-sm font-semibold mb-2">답변 완료</h2>
                                <p className="text-xl md:text-2xl font-bold text-[#388e3c]">{answeredQna}건</p>
                            </div>
                            <CheckCircle className="w-8 h-8 text-[#bfa06a]" />
                        </section>
                        <section className="bg-[#e9dec7] p-4 md:p-6 rounded-lg shadow-sm border border-[#bfa06a] flex items-center justify-between">
                            <div>
                                <h2 className="text-[#5b4636] text-sm font-semibold mb-2">미답변</h2>
                                <p className="text-xl md:text-2xl font-bold text-[#b94a48]">{unansweredQna}건</p>
                            </div>
                            <Clock className="w-8 h-8 text-[#bfa06a]" />
                        </section>
                    </div>

                    {/* 검색 및 필터 */}
                    <div className="bg-[#e9dec7] p-4 md:p-6 rounded-lg shadow-sm border border-[#bfa06a] mb-6">
                        <div className="flex flex-col md:flex-row gap-4">
                            <div className="flex-1 relative">
                                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-[#bfa06a] w-4 h-4" />
                                <input
                                    type="text"
                                    placeholder="제목, 내용, 고객명, 상품명으로 검색..."
                                    value={searchKeyword}
                                    onChange={(e) => setSearchKeyword(e.target.value)}
                                    className="w-full pl-10 pr-4 py-2 border border-[#bfa06a] rounded-lg bg-[#f5f1eb] text-[#5b4636] placeholder-[#bfa06a] focus:outline-none focus:ring-2 focus:ring-[#bfa06a]"
                                />
                            </div>
                            <div className="flex gap-2">
                                <select
                                    value={statusFilter}
                                    onChange={(e) => setStatusFilter(e.target.value)}
                                    className="px-4 py-2 border border-[#bfa06a] rounded-lg bg-[#f5f1eb] text-[#5b4636] focus:outline-none focus:ring-2 focus:ring-[#bfa06a]"
                                >
                                    <option value="">전체 상태</option>
                                    <option value="answered">답변 완료</option>
                                    <option value="unanswered">미답변</option>
                                </select>
                            </div>
                        </div>
                    </div>

                    {/* QnA 목록 */}
                    {error ? (
                        <div className="bg-[#fbeee0] border border-[#bfa06a] rounded-lg p-4">
                            <p className="text-[#b94a48]">{error}</p>
                        </div>
                    ) : filteredQnaList.length === 0 ? (
                        <div className="bg-[#e9dec7] border border-[#bfa06a] rounded-lg p-8 text-center">
                            <MessageCircle className="w-12 h-12 text-[#bfa06a] mx-auto mb-4" />
                            <p className="text-[#bfa06a] text-lg">고객 문의가 없습니다.</p>
                        </div>
                    ) : (
                        <div className="overflow-x-auto bg-[#e9dec7] rounded-lg shadow-sm border border-[#bfa06a]">
                            <table className="min-w-full divide-y divide-[#bfa06a]">
                                <thead className="bg-[#e9dec7]">
                                    <tr>
                                        <th className="px-6 py-3 text-left text-xs font-medium text-[#bfa06a] uppercase tracking-wider">고객/상품</th>
                                        <th className="px-6 py-3 text-left text-xs font-medium text-[#bfa06a] uppercase tracking-wider">제목</th>
                                        <th className="px-6 py-3 text-left text-xs font-medium text-[#bfa06a] uppercase tracking-wider">상태</th>
                                        <th className="px-6 py-3 text-left text-xs font-medium text-[#bfa06a] uppercase tracking-wider">등록일</th>
                                        <th className="px-6 py-3 text-left text-xs font-medium text-[#bfa06a] uppercase tracking-wider">답변일</th>
                                        <th className="px-6 py-3 text-center text-xs font-medium text-[#bfa06a] uppercase tracking-wider">액션</th>
                                    </tr>
                                </thead>
                                <tbody className="bg-[#e9dec7] divide-y divide-[#bfa06a]">
                                    {filteredQnaList.map((item) => {
                                        const qna = item.qna || item;
                                        return (
                                            <tr key={qna.id} className="hover:bg-[#bfa06a] transition-colors">
                                                <td className="px-6 py-4 whitespace-nowrap">
                                                    <div className="flex flex-col">
                                                        <div className="flex items-center gap-1 text-sm text-[#5b4636]">
                                                            <User className="w-3 h-3" />
                                                            <span className="font-medium">{qna.customerName}</span>
                                                        </div>
                                                        <div className="flex items-center gap-1 text-xs text-[#bfa06a]">
                                                            <Package className="w-3 h-3" />
                                                            <span className="truncate max-w-32">{qna.productName}</span>
                                                        </div>
                                                    </div>
                                                </td>
                                                <td className="px-6 py-4 whitespace-nowrap font-medium text-[#5b4636] max-w-xs truncate">
                                                    {qna.title}
                                                </td>
                                                <td className="px-6 py-4 whitespace-nowrap">
                                                    {(qna.answered === true || qna.answered === 'true') ? (
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
                                                </td>
                                                <td className="px-6 py-4 whitespace-nowrap text-sm text-[#5b4636]">
                                                    {qna.createdAt}
                                                </td>
                                                <td className="px-6 py-4 whitespace-nowrap text-sm text-[#5b4636]">
                                                    {qna.answeredAt || '-'}
                                                </td>
                                                <td className="px-6 py-4 whitespace-nowrap text-center">
                                                    <button
                                                        onClick={() => router.push(`/seller/qna/${qna.id}`)}
                                                        className="inline-flex items-center gap-1 bg-[#bfa06a] text-[#4b3a2f] px-3 py-1.5 rounded hover:bg-[#5b4636] hover:text-[#e9dec7] text-sm"
                                                    >
                                                        <Eye className="w-4 h-4" /> 상세 보기
                                                    </button>
                                                </td>
                                            </tr>
                                        );
                                    })}
                                </tbody>
                            </table>
                        </div>
                    )}

                    {/* 페이지네이션 */}
                    {totalPages > 1 && (
                        <div className="mt-6 flex justify-center">
                            <div className="flex gap-2">
                                <button
                                    onClick={() => setPage(Math.max(0, page - 1))}
                                    disabled={page === 0}
                                    className="px-3 py-2 border border-[#bfa06a] rounded bg-[#e9dec7] text-[#5b4636] disabled:opacity-50 disabled:cursor-not-allowed hover:bg-[#bfa06a] hover:text-[#4b3a2f]"
                                >
                                    이전
                                </button>
                                <span className="px-3 py-2 text-[#5b4636]">
                                    {page + 1} / {totalPages}
                                </span>
                                <button
                                    onClick={() => setPage(Math.min(totalPages - 1, page + 1))}
                                    disabled={page === totalPages - 1}
                                    className="px-3 py-2 border border-[#bfa06a] rounded bg-[#e9dec7] text-[#5b4636] disabled:opacity-50 disabled:cursor-not-allowed hover:bg-[#bfa06a] hover:text-[#4b3a2f]"
                                >
                                    다음
                                </button>
                            </div>
                        </div>
                    )}
                </div>
            </SellerLayout>
        </>
    );
}